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
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
      List<InspectionItems> inspections = persistenceService.run(
          Select.from(Inspection_.class)).listOf(InspectionItems.class);

      return ResponseEntity.ok(inspections);
    } catch (Exception e) {
      logger.error("Error fetching inspections", e);
      return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
  }

  /**
   * GET /api/inspections/{id} - Get inspection by ID
   */
  @GetMapping("/inspections/{id}")
  public ResponseEntity<?> getInspectionById(@PathVariable String id) {
    try {
      logger.info("Fetching inspection: {}", id);
      Optional<InspectionItems> inspection = persistenceService.run(
          Select.from(Inspection_.class)
              .where(i -> i.ID().eq(id)))
          .first(InspectionItems.class);

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
   * GET /api/equipment - Get all equipment
   */
  @GetMapping("/equipment")
  public ResponseEntity<?> getAllEquipment() {
    try {
      logger.info("Fetching all equipment");
      List<EquipmentItems> equipment = persistenceService.run(
          Select.from(Equipment_.class)).listOf(EquipmentItems.class);

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
      List<InspectorItems> inspectors = persistenceService.run(
          Select.from(Inspector_.class)).listOf(InspectorItems.class);

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
      @RequestParam String inspectionId,
      @RequestParam(required = false) String additionalComments) {
    try {
      logger.info("Generating report for inspection: {}", inspectionId);

      // Retrieve inspection with details
      Optional<InspectionItems> inspection = persistenceService.run(
          Select.from(Inspection_.class)
              .where(i -> i.ID().eq(inspectionId)))
          .first(InspectionItems.class);

      if (!inspection.isPresent()) {
        return ResponseEntity.notFound().build();
      }

      InspectionItems insp = inspection.get();

      // Get equipment details
      Optional<EquipmentItems> equipment = persistenceService.run(
          Select.from(Equipment_.class)
              .where(e -> e.ID().eq(insp.getEquipmentID())))
          .first(EquipmentItems.class);

      // Get inspector details
      Optional<InspectorItems> inspector = persistenceService.run(
          Select.from(Inspector_.class)
              .where(i -> i.ID().eq(insp.getInspectorID())))
          .first(InspectorItems.class);

      // Create response
      Map<String, Object> response = new LinkedHashMap<>();
      response.put("inspectionId", inspectionId);
      response.put("message", "Report generation initiated");
      response.put("equipment", equipment.map(EquipmentItems::getName).orElse("N/A"));
      response.put("inspector", inspector.map(InspectorItems::getName).orElse("N/A"));
      response.put("status", insp.getStatus());

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
  public ResponseEntity<?> exportReportPdf(@PathVariable String reportId) {
    try {
      logger.info("Exporting report as PDF: {}", reportId);

      Optional<InspectionReportItems> report = persistenceService.run(
          Select.from(InspectionReport_.class)
              .where(r -> r.ID().eq(reportId)))
          .first(InspectionReportItems.class);

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
      @RequestParam(required = false) String equipmentId,
      @RequestParam(required = false) String inspectorId,
      @RequestParam(required = false) String status) {
    try {
      logger.info("Searching inspections - Equipment: {}, Inspector: {}, Status: {}",
          equipmentId, inspectorId, status);

      List<InspectionItems> inspections = persistenceService.run(
          Select.from(Inspection_.class)).listOf(InspectionItems.class);

      List<InspectionItems> filtered = inspections.stream()
          .filter(i -> equipmentId == null || equipmentId.isEmpty() || equipmentId.equals(i.getEquipmentID()))
          .filter(i -> inspectorId == null || inspectorId.isEmpty() || inspectorId.equals(i.getInspectorID()))
          .filter(i -> status == null || status.isEmpty() || status.equals(i.getStatus()))
          .collect(Collectors.toList());

      return ResponseEntity.ok(filtered);

    } catch (Exception e) {
      logger.error("Error searching inspections", e);
      return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
  }
}
