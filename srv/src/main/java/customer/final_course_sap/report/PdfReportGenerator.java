package customer.final_course_sap.report;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFont;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PDF Report Generator for Equipment Inspection Reports
 * Generates professional inspection reports in PDF format using iText
 */
public class PdfReportGenerator {

  private static final Logger logger = LoggerFactory.getLogger(PdfReportGenerator.class);

  /**
   * Generate inspection report PDF
   * 
   * @param equipmentName       Name of the equipment
   * @param equipmentType       Type of equipment
   * @param location            Equipment location
   * @param serialNumber        Equipment serial number
   * @param inspectionDate      Date of inspection
   * @param completionDate      Completion date
   * @param inspectorName       Name of the inspector
   * @param inspectorDepartment Inspector department
   * @param status              Inspection status
   * @param findings            Inspection findings
   * @param safetyIssues        Safety issues found
   * @param notes               Additional notes
   * @param additionalComments  Additional comments
   * @param approvedBy          Approved by
   * @param approvalDate        Approval date
   * @return Base64 encoded PDF content
   */
  public static String generateInspectionReport(
      String equipmentName,
      String equipmentType,
      String location,
      String serialNumber,
      String inspectionDate,
      String completionDate,
      String inspectorName,
      String inspectorDepartment,
      String status,
      String findings,
      String safetyIssues,
      String notes,
      String additionalComments,
      String approvedBy,
      String approvalDate) {

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PdfWriter writer = new PdfWriter(baos);
      PdfDocument pdfDoc = new PdfDocument(writer);
      Document document = new Document(pdfDoc);

      // Set font
      PdfFont boldFont = PdfFontFactory.createFont(null, "Courier", true);
      PdfFont regularFont = PdfFontFactory.createFont(null, "Courier", false);

      // Title
      Paragraph title = new Paragraph("EQUIPMENT INSPECTION REPORT")
          .setFont(boldFont)
          .setFontSize(20)
          .setTextAlignment(TextAlignment.CENTER);
      document.add(title);

      document.add(new Paragraph("\n"));

      // Equipment Information Section
      Paragraph equipmentHeader = new Paragraph("EQUIPMENT INFORMATION")
          .setFont(boldFont)
          .setFontSize(14);
      document.add(equipmentHeader);

      Table equipmentTable = new Table(2);
      equipmentTable.setWidth(UnitValue.createPercentValue(100));

      equipmentTable.addCell(createCell("Equipment Name:", boldFont));
      equipmentTable.addCell(createCell(equipmentName != null ? equipmentName : "N/A", regularFont));

      equipmentTable.addCell(createCell("Equipment Type:", boldFont));
      equipmentTable.addCell(createCell(equipmentType != null ? equipmentType : "N/A", regularFont));

      equipmentTable.addCell(createCell("Location:", boldFont));
      equipmentTable.addCell(createCell(location != null ? location : "N/A", regularFont));

      equipmentTable.addCell(createCell("Serial Number:", boldFont));
      equipmentTable.addCell(createCell(serialNumber != null ? serialNumber : "N/A", regularFont));

      document.add(equipmentTable);
      document.add(new Paragraph("\n"));

      // Inspection Information Section
      Paragraph inspectionHeader = new Paragraph("INSPECTION DETAILS")
          .setFont(boldFont)
          .setFontSize(14);
      document.add(inspectionHeader);

      Table inspectionTable = new Table(2);
      inspectionTable.setWidth(UnitValue.createPercentValue(100));

      inspectionTable.addCell(createCell("Inspection Date:", boldFont));
      inspectionTable.addCell(createCell(inspectionDate != null ? inspectionDate : "N/A", regularFont));

      inspectionTable.addCell(createCell("Completion Date:", boldFont));
      inspectionTable.addCell(createCell(completionDate != null ? completionDate : "N/A", regularFont));

      inspectionTable.addCell(createCell("Status:", boldFont));
      inspectionTable.addCell(createCell(status != null ? status : "N/A", regularFont));

      document.add(inspectionTable);
      document.add(new Paragraph("\n"));

      // Inspector Information Section
      Paragraph inspectorHeader = new Paragraph("INSPECTOR INFORMATION")
          .setFont(boldFont)
          .setFontSize(14);
      document.add(inspectorHeader);

      Table inspectorTable = new Table(2);
      inspectorTable.setWidth(UnitValue.createPercentValue(100));

      inspectorTable.addCell(createCell("Inspector Name:", boldFont));
      inspectorTable.addCell(createCell(inspectorName != null ? inspectorName : "N/A", regularFont));

      inspectorTable.addCell(createCell("Department:", boldFont));
      inspectorTable.addCell(createCell(inspectorDepartment != null ? inspectorDepartment : "N/A", regularFont));

      document.add(inspectorTable);
      document.add(new Paragraph("\n"));

      // Findings Section
      Paragraph findingsHeader = new Paragraph("INSPECTION FINDINGS")
          .setFont(boldFont)
          .setFontSize(14);
      document.add(findingsHeader);

      Paragraph findingsContent = new Paragraph(findings != null ? findings : "No findings documented")
          .setFont(regularFont);
      document.add(findingsContent);
      document.add(new Paragraph("\n"));

      // Safety Issues Section
      if (safetyIssues != null && !safetyIssues.isEmpty()) {
        Paragraph safetyHeader = new Paragraph("SAFETY ISSUES")
            .setFont(boldFont)
            .setFontSize(14);
        document.add(safetyHeader);

        Paragraph safetyContent = new Paragraph(safetyIssues)
            .setFont(regularFont);
        document.add(safetyContent);
        document.add(new Paragraph("\n"));
      }

      // Additional Notes Section
      if (notes != null && !notes.isEmpty()) {
        Paragraph notesHeader = new Paragraph("ADDITIONAL NOTES")
            .setFont(boldFont)
            .setFontSize(14);
        document.add(notesHeader);

        Paragraph notesContent = new Paragraph(notes)
            .setFont(regularFont);
        document.add(notesContent);
        document.add(new Paragraph("\n"));
      }

      // Additional Comments Section
      if (additionalComments != null && !additionalComments.isEmpty()) {
        Paragraph commentsHeader = new Paragraph("ADDITIONAL COMMENTS")
            .setFont(boldFont)
            .setFontSize(14);
        document.add(commentsHeader);

        Paragraph commentsContent = new Paragraph(additionalComments)
            .setFont(regularFont);
        document.add(commentsContent);
        document.add(new Paragraph("\n"));
      }

      // Approval Section
      document.add(new Paragraph("\n"));
      Paragraph approvalHeader = new Paragraph("APPROVAL")
          .setFont(boldFont)
          .setFontSize(14);
      document.add(approvalHeader);

      Table approvalTable = new Table(2);
      approvalTable.setWidth(UnitValue.createPercentValue(100));

      approvalTable.addCell(createCell("Approved By:", boldFont));
      approvalTable.addCell(createCell(approvedBy != null ? approvedBy : "Pending", regularFont));

      approvalTable.addCell(createCell("Approval Date:", boldFont));
      approvalTable.addCell(createCell(approvalDate != null ? approvalDate : "Pending", regularFont));

      document.add(approvalTable);

      document.close();

      byte[] pdfBytes = baos.toByteArray();
      String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);

      logger.info("PDF report generated successfully for equipment: {}", equipmentName);
      return base64Pdf;

    } catch (Exception e) {
      logger.error("Error generating PDF report", e);
      throw new RuntimeException("Failed to generate PDF report: " + e.getMessage(), e);
    }
  }

  /**
   * Helper method to create table cells
   */
  private static Cell createCell(String text, PdfFont font) {
    Paragraph p = new Paragraph(text).setFont(font);
    return new Cell().add(p);
  }
}
