package customer.final_course_sap.handlers;

import com.sap.cds.ql.Insert;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cds.gen.equipment.inspection.InspectionApproval;
import cds.gen.equipment.inspection.InspectionApproval_;
import cds.gen.equipment.inspection.InspectionComment;
import cds.gen.equipment.inspection.InspectionComment_;
import cds.gen.inspectionservice.*;
import customer.final_course_sap.report.PdfReportGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@ServiceName(InspectionService_.CDS_NAME)
public class InspectionServiceHandler implements EventHandler {

  private static final Logger logger = LoggerFactory.getLogger(InspectionServiceHandler.class);
  private final PersistenceService persistenceService;

  public InspectionServiceHandler(PersistenceService persistenceService) {
    this.persistenceService = persistenceService;
  }

  @On(event = SearchInspectionsContext.CDS_NAME)
  public void handleSearchInspections(SearchInspectionsContext context) {
    String search = context.getSearch();
    String status = context.getStatus();
    String equipmentId = context.getEquipmentId();
    String inspectorId = context.getInspectorId();
    LocalDate inspectionDateFrom = context.getInspectionDateFrom();
    LocalDate inspectionDateTo = context.getInspectionDateTo();
    String equipmentName = context.getEquipmentName();
    Integer limit = context.getLimit() != null ? context.getLimit() : 100;
    Integer offset = context.getOffset() != null ? context.getOffset() : 0;

    logger.info("Search inspections with filters: status={}, inspectorId={}", status, inspectorId);

    try {
      CqnSelect select = Select.from(Inspection_.class)
          .columns(i -> i.ID(),
              i -> i.inspectionDate(),
              i -> i.status(),
              i -> i.findings(),
              i -> i.equipment().expand(e -> e.name(), e -> e.type()),
              i -> i.inspector().expand(ins -> ins.name()))
          .where(i -> {
            List<com.sap.cds.ql.cqn.CqnPredicate> predicates = new ArrayList<>();

            if (equipmentId != null && !equipmentId.isEmpty()) {
              predicates.add(i.equipment_ID().eq(equipmentId));
            }
            if (inspectorId != null && !inspectorId.isEmpty()) {
              predicates.add(i.inspector_ID().eq(inspectorId));
            }
            if (status != null && !status.isEmpty()) {
              predicates.add(i.status().eq(status));
            }
            if (inspectionDateFrom != null) {
              predicates.add(i.inspectionDate().ge(inspectionDateFrom));
            }
            if (inspectionDateTo != null) {
              predicates.add(i.inspectionDate().le(inspectionDateTo));
            }
            if (equipmentName != null && !equipmentName.isEmpty()) {
              predicates.add(i.equipment().name().contains(equipmentName));
            }

            if (predicates.isEmpty()) {
              return i.ID().isNotNull();
            }

            return com.sap.cds.ql.CQL.and(predicates);
          })
          .orderBy(i -> i.inspectionDate().desc());
      List<Inspection> inspections = persistenceService.run(select).listOf(Inspection.class);
      List<InspectionSearchResult> results = inspections.stream()
          .map(this::toSearchResult)
          .toList();

      context.setResult(results);

    } catch (Exception e) {
      logger.error("Error during searchInspections", e);
      throw new ServiceException("Failed to search inspections", e);
    }
  }

  private InspectionSearchResult toSearchResult(Inspection inspection) {
    InspectionSearchResult result = InspectionSearchResult.create();
    result.setId(inspection.getId());
    result.setInspectionDate(inspection.getInspectionDate());
    result.setStatus(inspection.getStatus());
    result.setFindings(inspection.getFindings());

    // Enrich from expanded data
    if (inspection.getEquipment() != null) {
      result.setEquipmentName(inspection.getEquipment().getName());
      result.setEquipmentType(inspection.getEquipment().getType());
    }
    if (inspection.getInspector() != null) {
      result.setInspectorName(inspection.getInspector().getName());
    }

    return result;
  }

  @On(event = GetInspectionDetailsContext.CDS_NAME)
  public void handleGetInspectionDetails(GetInspectionDetailsContext context) {
    String inspectionId = context.getInspectionId();

    try {
      Optional<Inspection> opt = persistenceService.run(
          Select.from(Inspection_.class)
              .where(i -> i.ID().eq(inspectionId))
              .columns(i -> i._all(),
                  i -> i.equipment().expand(),
                  i -> i.inspector().expand()))
          .first(Inspection.class);

      Inspection insp = opt.orElseThrow(() -> new ServiceException("Inspection not found: " + inspectionId));

      GetInspectionDetailsContext.ReturnType details = GetInspectionDetailsContext.ReturnType.create();

      details.setInspectionId(insp.getId());
      details.setInspectionDate(insp.getInspectionDate());
      details.setCompletionDate(insp.getCompletionDate());
      details.setStatus(insp.getStatus());
      details.setFindings(insp.getFindings());
      details.setSafetyIssues(insp.getSafetyIssues());
      details.setNotes(insp.getNotes());

      if (insp.getEquipment() != null) {
        details.setEquipmentName(insp.getEquipment().getName());
        details.setEquipmentType(insp.getEquipment().getType());
        details.setEquipmentLocation(insp.getEquipment().getLocation());
        details.setSerialNumber(insp.getEquipment().getSerialNumber());
      }

      if (insp.getInspector() != null) {
        details.setInspectorName(insp.getInspector().getName());
        details.setInspectorDepartment(insp.getInspector().getDepartment());
      }

      context.setResult(details);

    } catch (Exception e) {
      logger.error("Error getting inspection details {}", inspectionId, e);
      throw new ServiceException("Failed to get inspection details", e);
    }
  }

  @On(event = GenerateReportContext.CDS_NAME)
  public void handleGenerateReport(GenerateReportContext context) {
    String inspectionId = context.getInspectionId();
    String additionalComments = context.getAdditionalComments() != null ? context.getAdditionalComments() : "";

    try {
      Inspection insp = persistenceService.run(
          Select.from(Inspection_.class)
              .where(i -> i.ID().eq(inspectionId))
              .columns(i -> i._all(), i -> i.equipment().expand(), i -> i.inspector().expand()))
          .first(Inspection.class)
          .orElseThrow(() -> new ServiceException("Inspection not found: " + inspectionId));

      String approvedBy = "Pending";
      String approvalDate = "Pending";

      Optional<InspectionApproval> approvalOpt = persistenceService.run(
          Select.from(InspectionApproval_.class)
              .where(a -> a.inspection_ID().eq(inspectionId))
              .orderBy(a -> a.approvalDate().desc()))
          .first(InspectionApproval.class);

      if (approvalOpt.isPresent()) {
        approvedBy = approvalOpt.get().getApprovedBy();
        approvalDate = approvalOpt.get().getApprovalDate().toString();
      }

      byte[] pdfBytes = PdfReportGenerator.generateInspectionReport(
          insp.getEquipment() != null ? insp.getEquipment().getName() : "N/A",
          insp.getEquipment() != null ? insp.getEquipment().getType() : "N/A",
          insp.getEquipment() != null ? insp.getEquipment().getLocation() : "N/A",
          insp.getEquipment() != null ? insp.getEquipment().getSerialNumber() : "N/A",
          insp.getInspectionDate() != null ? insp.getInspectionDate().toString() : "N/A",
          insp.getCompletionDate() != null ? insp.getCompletionDate().toString() : "N/A",
          insp.getInspector() != null ? insp.getInspector().getName() : "N/A",
          insp.getInspector() != null ? insp.getInspector().getDepartment() : "N/A",
          insp.getStatus(),
          insp.getFindings(),
          insp.getSafetyIssues(),
          insp.getNotes(),
          additionalComments,
          approvedBy,
          approvalDate);

      context.setResult(pdfBytes);
    } catch (Exception e) {
      logger.error("Failed to generate report", e);
      throw new ServiceException("Failed to generate PDF report", e);
    }
  }

  @On(event = ExportReportPdfContext.CDS_NAME)
  public void handleExportReportPdf(ExportReportPdfContext context) {
    String reportId = context.getReportId();
    try {
      InspectionReport report = persistenceService.run(
          Select.from(InspectionReport_.class).where(r -> r.ID().eq(reportId)))
          .first(InspectionReport.class)
          .orElseThrow(() -> new ServiceException("Report not found"));

      if (report.getReportPdf() == null) {
        throw new ServiceException("PDF content is missing for this report record.");
      }

      context.setResult(report.getReportPdf());
    } catch (ServiceException e) {
      throw e;
    } catch (Exception e) {
      throw new ServiceException("Error exporting PDF", e);
    }
  }

  @On(event = AddCommentContext.CDS_NAME)
  public void handleAddComment(AddCommentContext context) {
    try {
      InspectionComment comment = InspectionComment.create();
      comment.setInspectionId(context.getInspectionId());
      comment.setCommentText(context.getCommentText());
      comment.setCreatedAt(java.time.Instant.now());
      comment.setCreatedBy(context.getUserInfo().getName());

      persistenceService.run(Insert.into(InspectionComment_.class).entry(comment));
      context.setResult(true);
    } catch (Exception e) {
      throw new ServiceException("Failed to add comment", e);
    }
  }

  @On(event = ApproveInspectionContext.CDS_NAME)
  public void handleApproveInspection(ApproveInspectionContext context) {
    String inspectionId = context.getInspectionId();
    try {
      persistenceService.run(Update.entity(Inspection_.class)
          .where(i -> i.ID().eq(inspectionId))
          .data(Inspection.STATUS, "Approved"));

      InspectionApproval approval = InspectionApproval.create();
      approval.setInspectionId(inspectionId);
      approval.setApprovedBy(context.getUserInfo().getName());
      approval.setApprovalDate(java.time.Instant.now());
      approval.setNote(context.getApprovalNote());

      persistenceService.run(Insert.into(InspectionApproval_.class).entry(approval));
      context.setResult(true);
    } catch (Exception e) {
      throw new ServiceException("Approval failed", e);
    }
  }

  @On(event = PrintInspectionsContext.CDS_NAME)
  public void handlePrintInspections(PrintInspectionsContext context) {
    Collection<String> rawIds = context.getInspectionIds();
    List<String> inspectionIds = rawIds == null
        ? new ArrayList<>()
        : new ArrayList<>(rawIds);

    logger.info("Printing {} inspections", inspectionIds.size());

    try {
      List<Inspection> inspections = new ArrayList<>();
      for (String id : inspectionIds) {
        if (id == null || id.isBlank()) {
          continue;
        }
        Inspection insp = persistenceService.run(
            Select.from(Inspection_.class)
                .where(i -> i.ID().eq(id))
                .columns(i -> i._all(), i -> i.equipment().expand(), i -> i.inspector().expand()))
            .first(Inspection.class)
            .orElse(null);
        if (insp != null) {
          inspections.add(insp);
        }
      }

      // Generate PDF with all inspections
      byte[] pdfBytes = PdfReportGenerator.generateMultipleInspectionsReport(inspections);

      context.setResult(pdfBytes);
    } catch (Exception e) {
      logger.error("Failed to print inspections", e);
      throw new ServiceException("Failed to generate print report", e);
    }
  }
}