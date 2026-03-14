package customer.final_course_sap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.cds.services.persistence.PersistenceService;
import cds.gen.inspectionservice.*;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Delete;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.time.LocalDate;

/**
 * REST Controller for Equipment Inspection Reports
 * Provides endpoints for searching inspections and downloading reports
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class InspectionController {

  private static final Logger logger = LoggerFactory.getLogger(InspectionController.class);

  @Autowired
  private PersistenceService persistenceService;

  /**
   * GET /api/inspections - Get all inspections
   */
  @GetMapping("/inspections")
  public ResponseEntity<?> getAllInspections() {
    try {
      logger.info("Fetching all inspections");
      List<Inspection> inspections = persistenceService.run(
          Select.from(Inspection_.class)).listOf(Inspection.class);

      List<Map<String, Object>> result = inspections.stream()
          .map(this::toInspectionView)
          .collect(Collectors.toList());

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      logger.error("Error fetching inspections", e);
      return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
  }

  /**
   * GET /api/inspections/{id} - Get inspection by ID
   */
  @GetMapping("/inspections/{id}")
  public ResponseEntity<?> getInspectionById(@PathVariable("id") String id) {
    try {
      logger.info("Fetching inspection: {}", id);
      Optional<Inspection> inspection = persistenceService.run(
          Select.from(Inspection_.class)
              .where(i -> i.ID().eq(id)))
          .first(Inspection.class);

      if (inspection.isPresent()) {
        return ResponseEntity.ok(inspection.get());
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      logger.error("Error fetching inspection: {}", id, e);
      return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
  }

  /**
   * DELETE /api/inspections/{id} - Delete inspection by ID
   */
  @DeleteMapping("/inspections/{id}")
  public ResponseEntity<?> deleteInspection(@PathVariable("id") String id) {
    try {
      logger.info("Deleting inspection: {}", id);

      persistenceService.run(
          Delete.from(Inspection_.class)
              .where(i -> i.ID().eq(id)));

      return ResponseEntity.noContent().build();

    } catch (Exception e) {
      logger.error("Error deleting inspection: {}", id, e);
      return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
  }

  /**
   * GET /api/equipment - Get all equipment
   */
  @GetMapping("/equipment")
  public ResponseEntity<?> getAllEquipment() {
    try {
      logger.info("Fetching all equipment");
      List<Equipment> equipment = persistenceService.run(
          Select.from(Equipment_.class)).listOf(Equipment.class);

      return ResponseEntity.ok(equipment);
    } catch (Exception e) {
      logger.error("Error fetching equipment", e);
      return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
  }

  /**
   * GET /api/inspectors - Get all inspectors
   */
  @GetMapping("/inspectors")
  public ResponseEntity<?> getAllInspectors() {
    try {
      logger.info("Fetching all inspectors");
      List<Inspector> inspectors = persistenceService.run(
          Select.from(Inspector_.class)).listOf(Inspector.class);

      return ResponseEntity.ok(inspectors);
    } catch (Exception e) {
      logger.error("Error fetching inspectors", e);
      return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
  }

  /**
   * POST /api/reports/generate - Generate inspection report
   */
  @PostMapping("/reports/generate")
  public ResponseEntity<?> generateReport(
      @RequestBody Map<String, Object> request) {
    try {
      String inspectionId = (String) request.getOrDefault("inspectionId",
          "INS-" + System.currentTimeMillis());
      String equipmentId = (String) request.get("equipmentId");
      String inspectorId = (String) request.get("inspectorId");
      String inspectionDateStr = (String) request.get("inspectionDate");
      String completionDateStr = (String) request.get("completionDate");
      String status = (String) request.getOrDefault("status", "PENDING");
      String findings = (String) request.get("findings");
      String safetyIssues = (String) request.get("safetyIssues");
      String notes = (String) request.get("notes");

      logger.info("Generating report for inspection: {}", inspectionId);

      // Create new inspection record
      Map<String, Object> data = new LinkedHashMap<>();
      data.put("ID", inspectionId);
      data.put("equipment_ID", equipmentId);
      data.put("inspector_ID", inspectorId);

      if (inspectionDateStr != null && !inspectionDateStr.isEmpty()) {
        data.put("inspectionDate", LocalDate.parse(inspectionDateStr));
      }
      if (completionDateStr != null && !completionDateStr.isEmpty()) {
        data.put("completionDate", LocalDate.parse(completionDateStr));
      }

      data.put("status", status);
      data.put("findings", findings);
      data.put("safetyIssues", safetyIssues);
      data.put("notes", notes);

      persistenceService.run(com.sap.cds.ql.Insert.into(Inspection_.class).entry(data));

      // Load related data for response
      Optional<Equipment> equipment = persistenceService.run(
          Select.from(Equipment_.class)
              .where(e -> e.ID().eq(equipmentId)))
          .first(Equipment.class);

      Optional<Inspector> inspector = persistenceService.run(
          Select.from(Inspector_.class)
              .where(i -> i.ID().eq(inspectorId)))
          .first(Inspector.class);

      // Create response
      Map<String, Object> response = new LinkedHashMap<>();
      response.put("inspectionId", inspectionId);
      response.put("message", "Inspection created successfully");
      response.put("equipment", equipment.map(Equipment::getName).orElse("N/A"));
      response.put("inspector", inspector.map(Inspector::getName).orElse("N/A"));
      response.put("status", status);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("Error generating report", e);
      return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
  }

  /**
   * GET /api/reports/export/{reportId} - Export report as PDF
   */
  @GetMapping("/reports/export/{reportId}")
  public ResponseEntity<?> exportReportPdf(@PathVariable("reportId") String reportId) {
    try {
      logger.info("Exporting report as PDF: {}", reportId);

      Optional<InspectionReport> report = persistenceService.run(
          Select.from(InspectionReport_.class)
              .where(r -> r.ID().eq(reportId)))
          .first(InspectionReport.class);

      if (!report.isPresent()) {
        return ResponseEntity.notFound().build();
      }

      String base64Pdf = report.get().getReportPdf();
      byte[] pdfBytes = Base64.getDecoder().decode(base64Pdf);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDispositionFormData("attachment", "inspection_report_" + reportId + ".pdf");
      headers.setContentLength(pdfBytes.length);

      return ResponseEntity.ok()
          .headers(headers)
          .body(pdfBytes);

    } catch (Exception e) {
      logger.error("Error exporting report PDF: {}", reportId, e);
      return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
  }

  /**
   * GET /api/search - Search inspections with filtering
   */
  @GetMapping("/search")
  public ResponseEntity<?> searchInspections(
      @RequestParam(value = "equipmentName", required = false) String equipmentName,
      @RequestParam(value = "inspectorId", required = false) String inspectorId,
      @RequestParam(value = "dateFrom", required = false) String dateFrom,
      @RequestParam(value = "dateTo", required = false) String dateTo,
      @RequestParam(value = "status", required = false) String status) {
    try {
      logger.info("Searching inspections - Equipment: {}, Inspector: {}, Status: {}",
          equipmentName, inspectorId, status);

      List<Inspection> inspections = persistenceService.run(
          Select.from(Inspection_.class)).listOf(Inspection.class);

      List<Map<String, Object>> views = inspections.stream()
          .map(this::toInspectionView)
          .collect(Collectors.toList());

      List<Map<String, Object>> filtered = views.stream()
          // Filter by equipment name (contains, case-insensitive)
          .filter(v -> {
            if (equipmentName == null || equipmentName.isEmpty()) {
              return true;
            }
            String eqName = (String) v.getOrDefault("equipmentName", "");
            return eqName.toLowerCase().contains(equipmentName.toLowerCase());
          })
          // Filter by inspector
          .filter(v -> {
            if (inspectorId == null || inspectorId.isEmpty()) {
              return true;
            }
            String inspId = (String) v.getOrDefault("inspectorId", "");
            return inspectorId.equals(inspId);
          })
          // Filter by date range
          .filter(v -> {
            if ((dateFrom == null || dateFrom.isEmpty()) && (dateTo == null || dateTo.isEmpty())) {
              return true;
            }
            Object dateObj = v.get("inspectionDate");
            if (dateObj == null) {
              return false;
            }
            LocalDate d = (LocalDate) dateObj;
            if (dateFrom != null && !dateFrom.isEmpty()) {
              LocalDate from = LocalDate.parse(dateFrom);
              if (d.isBefore(from)) {
                return false;
              }
            }
            if (dateTo != null && !dateTo.isEmpty()) {
              LocalDate to = LocalDate.parse(dateTo);
              if (d.isAfter(to)) {
                return false;
              }
            }
            return true;
          })
          // Filter by status
          .filter(v -> {
            if (status == null || status.isEmpty()) {
              return true;
            }
            String s = (String) v.getOrDefault("status", "");
            return status.equalsIgnoreCase(s);
          })
          .collect(Collectors.toList());

      return ResponseEntity.ok(filtered);

    } catch (Exception e) {
      logger.error("Error searching inspections", e);
      return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
  }

  /**
   * Build a view model for inspections with equipment/inspector names
   */
  private Map<String, Object> toInspectionView(Inspection insp) {
    Map<String, Object> view = new LinkedHashMap<>();
    view.put("ID", insp.getId());
    view.put("inspectionDate", insp.getInspectionDate());
    view.put("status", insp.getStatus());
    view.put("findings", insp.getFindings());
    view.put("inspectorId", insp.getInspectorId());

    try {
      Optional<Equipment> equipment = persistenceService.run(
          Select.from(Equipment_.class)
              .where(e -> e.ID().eq(insp.getEquipmentId())))
          .first(Equipment.class);

      Optional<Inspector> inspector = persistenceService.run(
          Select.from(Inspector_.class)
              .where(i -> i.ID().eq(insp.getInspectorId())))
          .first(Inspector.class);

      view.put("equipmentName", equipment.map(Equipment::getName).orElse(null));
      view.put("inspectorName", inspector.map(Inspector::getName).orElse(null));
    } catch (Exception e) {
      logger.warn("Unable to enrich inspection {} with equipment/inspector data", insp.getId(), e);
    }

    return view;
  }
}
