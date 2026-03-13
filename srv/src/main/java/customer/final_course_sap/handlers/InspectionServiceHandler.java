package customer.final_course_sap.handlers;

import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.cds.CdsUpdateEventContext;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.HandlerOrder;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cds.gen.inspectionservice.*;
import customer.final_course_sap.report.PdfReportGenerator;
import com.sap.cds.ql.Select;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service handler for Equipment Inspection Service
 * Implements search, filtering, and report generation logic
 */
@Component
@ServiceName("InspectionService")
public class InspectionServiceHandler {

  private static final Logger logger = LoggerFactory.getLogger(InspectionServiceHandler.class);
  private final PersistenceService persistenceService;

  public InspectionServiceHandler(PersistenceService persistenceService) {
    this.persistenceService = persistenceService;
  }

  /**
   * Handle searchInspections function call
   * Search and filter inspection records based on criteria
   */
  @On(event = "searchInspections")
  public List<InspectionSearchResult> handleSearchInspections(SearchInspectionsContext context) {
    logger.info("Searching inspections with parameters - Equipment: {}, Inspector: {}, Status: {}",
        context.getEquipmentId(), context.getInspectorId(), context.getStatus());

    try {
      // Build query based on provided filters
      Select<?> query = Select.from(Inspection_.class);

      List<Inspection> inspections = persistenceService.run(query).listOf(Inspection.class);

      // Filter results based on parameters
      List<Inspection> filtered = inspections.stream()
          .filter(i -> filterByEquipmentId(i, context.getEquipmentId()))
          .filter(i -> filterByEquipmentName(i, context.getEquipmentName()))
          .filter(i -> filterByDateRange(i, context.getInspectionDateFrom(), context.getInspectionDateTo()))
          .filter(i -> filterByInspectorId(i, context.getInspectorId()))
          .filter(i -> filterByStatus(i, context.getStatus()))
          .collect(Collectors.toList());

      // Convert to search result format
      return filtered.stream()
          .map(this::toSearchResult)
          .collect(Collectors.toList());

    } catch (Exception e) {
      logger.error("Error searching inspections", e);
      throw new ServiceException("Failed to search inspections: " + e.getMessage());
    }
  }

  /**
   * Handle generateReport function call
   * Generate inspection report based on inspection data
   */
  @On(event = "generateReport")
  public ReportGenerationResponse handleGenerateReport(GenerateReportContext context) {
    String inspectionId = context.getInspectionId();
    String additionalComments = context.getAdditionalComments();

    logger.info("Generating report for inspection: {}", inspectionId);

    try {
      // Retrieve inspection details
      Optional<Inspection> inspection = persistenceService.run(
          Select.from(Inspection_.class)
              .where(i -> i.ID().eq(inspectionId)))
          .first(Inspection.class);

      if (!inspection.isPresent()) {
        logger.warn("Inspection not found: {}", inspectionId);
        throw new ServiceException("Inspection not found: " + inspectionId);
      }

      Inspection insp = inspection.get();

      // Get equipment details
      String equipmentId = insp.getEquipmentId();
      Optional<Equipment> equipment = persistenceService.run(
          Select.from(Equipment_.class)
              .where(e -> e.ID().eq(equipmentId)))
          .first(Equipment.class);

      // Get inspector details
      String inspectorId = insp.getInspectorId();
      Optional<Inspector> inspector = persistenceService.run(
          Select.from(Inspector_.class)
              .where(i -> i.ID().eq(inspectorId)))
          .first(Inspector.class);

      // Generate PDF
      String pdfContent = PdfReportGenerator.generateInspectionReport(
          equipment.map(Equipment::getName).orElse("N/A"),
          equipment.map(Equipment::getType).orElse("N/A"),
          equipment.map(Equipment::getLocation).orElse("N/A"),
          equipment.map(Equipment::getSerialNumber).orElse("N/A"),
          insp.getInspectionDate() != null ? insp.getInspectionDate().toString() : "N/A",
          insp.getCompletionDate() != null ? insp.getCompletionDate().toString() : "N/A",
          inspector.map(Inspector::getName).orElse("N/A"),
          inspector.map(Inspector::getDepartment).orElse("N/A"),
          insp.getStatus(),
          insp.getFindings(),
          insp.getSafetyIssues(),
          insp.getNotes(),
          additionalComments,
          null,
          null);

      // Create InspectionReport record
      InspectionReport report = InspectionReport.create();
      report.setInspectionId(inspectionId);
      report.setAdditionalComments(additionalComments);
      report.setReportPdf(pdfContent);
      report.setExportedAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());

      // Save report
      persistenceService.run(com.sap.cds.ql.Insert.into(InspectionReport_.class)
          .entry(report));

      logger.info("Report generated successfully for inspection: {}", inspectionId);

      ReportGenerationResponse response = ReportGenerationResponse.create();
      response.setReportId(report.getId());
      response.setPdfUrl("/api/reports/export/" + report.getId());
      response.setMessage("Report generated successfully");

      return response;

    } catch (Exception e) {
      logger.error("Error generating report for inspection: " + inspectionId, e);
      throw new ServiceException("Failed to generate report: " + e.getMessage());
    }
  }

  /**
   * Handle exportReportPdf function call
   * Export report as base64 encoded PDF
   */
  @On(event = "exportReportPdf")
  public String handleExportReportPdf(ExportReportPdfContext context) {
    String reportId = context.getReportId();
    logger.info("Exporting PDF for report: {}", reportId);

    try {
      Optional<InspectionReport> report = persistenceService.run(
          Select.from(InspectionReport_.class)
              .where(r -> r.ID().eq(reportId)))
          .first(InspectionReport.class);

      if (!report.isPresent()) {
        throw new ServiceException("Report not found: " + reportId);
      }

      return report.get().getReportPdf();

    } catch (Exception e) {
      logger.error("Error exporting report PDF: " + reportId, e);
      throw new ServiceException("Failed to export PDF: " + e.getMessage());
    }
  }

  /**
   * Handle getInspectionDetails function call
   * Get complete inspection details with related data
   */
  @On(event = "getInspectionDetails")
  public Map<String, Object> handleGetInspectionDetails(GetInspectionDetailsContext context) {
    String inspectionId = context.getInspectionId();
    logger.info("Retrieving details for inspection: {}", inspectionId);

    try {
      Optional<Inspection> inspection = persistenceService.run(
          Select.from(Inspection_.class)
              .where(i -> i.ID().eq(inspectionId)))
          .first(Inspection.class);

      if (!inspection.isPresent()) {
        throw new ServiceException("Inspection not found: " + inspectionId);
      }

      Inspection insp = inspection.get();

      // Get equipment details
      Optional<Equipment> equipment = persistenceService.run(
          Select.from(Equipment_.class)
              .where(e -> e.ID().eq(insp.getEquipmentId())))
          .first(Equipment.class);

      // Get inspector details
      Optional<Inspector> inspector = persistenceService.run(
          Select.from(Inspector_.class)
              .where(i -> i.ID().eq(insp.getInspectorId())))
          .first(Inspector.class);

      // Build response
      Map<String, Object> details = new LinkedHashMap<>();
      details.put("inspectionId", insp.getId());
      details.put("equipmentName", equipment.map(Equipment::getName).orElse("N/A"));
      details.put("equipmentType", equipment.map(Equipment::getType).orElse("N/A"));
      details.put("equipmentLocation", equipment.map(Equipment::getLocation).orElse("N/A"));
      details.put("serialNumber", equipment.map(Equipment::getSerialNumber).orElse("N/A"));
      details.put("inspectionDate", insp.getInspectionDate());
      details.put("completionDate", insp.getCompletionDate());
      details.put("inspectorName", inspector.map(Inspector::getName).orElse("N/A"));
      details.put("inspectorDepartment", inspector.map(Inspector::getDepartment).orElse("N/A"));
      details.put("status", insp.getStatus());
      details.put("findings", insp.getFindings());
      details.put("safetyIssues", insp.getSafetyIssues());
      details.put("notes", insp.getNotes());

      return details;

    } catch (Exception e) {
      logger.error("Error retrieving inspection details: " + inspectionId, e);
      throw new ServiceException("Failed to retrieve details: " + e.getMessage());
    }
  }

  // Helper methods for filtering

  private boolean filterByEquipmentId(Inspection inspection, String equipmentId) {
    if (equipmentId == null || equipmentId.isEmpty()) {
      return true;
    }
    return equipmentId.equals(inspection.getEquipmentId());
  }

  private boolean filterByEquipmentName(Inspection inspection, String equipmentName) {
    if (equipmentName == null || equipmentName.isEmpty()) {
      return true;
    }
    // This would require joining with Equipment entity - simplified for now
    return true;
  }

  private boolean filterByDateRange(Inspection inspection, LocalDate from, LocalDate to) {
    if (from == null && to == null) {
      return true;
    }
    LocalDate inspDate = inspection.getInspectionDate();
    if (inspDate == null) {
      return false;
    }
    if (from != null && inspDate.isBefore(from)) {
      return false;
    }
    if (to != null && inspDate.isAfter(to)) {
      return false;
    }
    return true;
  }

  private boolean filterByInspectorId(Inspection inspection, String inspectorId) {
    if (inspectorId == null || inspectorId.isEmpty()) {
      return true;
    }
    return inspectorId.equals(inspection.getInspectorId());
  }

  private boolean filterByStatus(Inspection inspection, String status) {
    if (status == null || status.isEmpty()) {
      return true;
    }
    return status.equals(inspection.getStatus());
  }

  private InspectionSearchResult toSearchResult(Inspection inspection) {
    InspectionSearchResult result = InspectionSearchResult.create();
    result.setId(inspection.getId());
    result.setInspectionDate(inspection.getInspectionDate());
    result.setStatus(inspection.getStatus());
    result.setFindings(inspection.getFindings());
    // Equipment and Inspector names would need to be fetched separately
    return result;
  }
}
