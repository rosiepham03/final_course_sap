package customer.final_course_sap.report;

import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

/**
 * PDF Report Generator for Equipment Inspection Reports
 * Trả về byte array trực tiếp để tối ưu hiệu năng.
 */
public class PdfReportGenerator {

        private static final Logger logger = LoggerFactory.getLogger(PdfReportGenerator.class);

        public static byte[] generateInspectionReport(
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

                // Sử dụng try-with-resources để đảm bảo đóng stream tự động
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        PdfWriter writer = new PdfWriter(baos);
                        PdfDocument pdfDoc = new PdfDocument(writer);
                        Document document = new Document(pdfDoc);

                        // Fonts
                        PdfFont regularFont = PdfFontFactory.createFont("Helvetica");
                        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

                        // Title
                        document.add(new Paragraph("EQUIPMENT INSPECTION REPORT")
                                        .setFont(boldFont)
                                        .setFontSize(20)
                                        .setTextAlignment(TextAlignment.CENTER));

                        document.add(new Paragraph("\n"));

                        // --- Section: Equipment Information ---
                        addSectionHeader(document, "EQUIPMENT INFORMATION", boldFont);
                        Table equipmentTable = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
                                        .useAllAvailableWidth();

                        addTableRow(equipmentTable, "Equipment Name:", equipmentName, boldFont, regularFont);
                        addTableRow(equipmentTable, "Equipment Type:", equipmentType, boldFont, regularFont);
                        addTableRow(equipmentTable, "Location:", location, boldFont, regularFont);
                        addTableRow(equipmentTable, "Serial Number:", serialNumber, boldFont, regularFont);

                        document.add(equipmentTable);
                        document.add(new Paragraph("\n"));

                        // --- Section: Inspection Details ---
                        addSectionHeader(document, "INSPECTION DETAILS", boldFont);
                        Table inspectionTable = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
                                        .useAllAvailableWidth();

                        addTableRow(inspectionTable, "Inspection Date:", inspectionDate, boldFont, regularFont);
                        addTableRow(inspectionTable, "Completion Date:", completionDate, boldFont, regularFont);
                        addTableRow(inspectionTable, "Status:", status, boldFont, regularFont);

                        document.add(inspectionTable);
                        document.add(new Paragraph("\n"));

                        // --- Section: Findings & Issues ---
                        addSectionHeader(document, "INSPECTION FINDINGS", boldFont);
                        document.add(new Paragraph(findings != null ? findings : "No findings documented")
                                        .setFont(regularFont));

                        if (safetyIssues != null && !safetyIssues.isEmpty()) {
                                addSectionHeader(document, "SAFETY ISSUES", boldFont);
                                document.add(new Paragraph(safetyIssues).setFont(regularFont));
                        }

                        // --- Section: Approval ---
                        document.add(new Paragraph("\n"));
                        addSectionHeader(document, "APPROVAL", boldFont);
                        Table approvalTable = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
                                        .useAllAvailableWidth();

                        addTableRow(approvalTable, "Approved By:", approvedBy != null ? approvedBy : "Pending",
                                        boldFont,
                                        regularFont);
                        addTableRow(approvalTable, "Approval Date:", approvalDate != null ? approvalDate : "Pending",
                                        boldFont,
                                        regularFont);

                        document.add(approvalTable);

                        document.close();

                        logger.info("PDF report generated successfully for: {}", equipmentName);
                        return baos.toByteArray();

                } catch (Exception e) {
                        logger.error("Error generating PDF report", e);
                        throw new RuntimeException("Failed to generate PDF report", e);
                }
        }

        private static void addSectionHeader(Document doc, String title, PdfFont font) {
                doc.add(new Paragraph(title).setFont(font).setFontSize(14).setUnderline());
        }

        private static void addTableRow(Table table, String label, String value, PdfFont bold, PdfFont reg) {
                table.addCell(new Cell().add(new Paragraph(label).setFont(bold)).setBorder(null));
                table.addCell(new Cell().add(new Paragraph(value != null ? value : "N/A").setFont(reg))
                                .setBorder(null));
        }

        public static byte[] generateMultipleInspectionsReport(
                        java.util.List<cds.gen.inspectionservice.Inspection> inspections) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        PdfWriter writer = new PdfWriter(baos);
                        PdfDocument pdfDoc = new PdfDocument(writer);
                        Document document = new Document(pdfDoc);

                        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
                        PdfFont regularFont = PdfFontFactory.createFont("Helvetica");

                        // Title
                        Paragraph title = new Paragraph("EQUIPMENT INSPECTION REPORT - BATCH")
                                        .setFont(boldFont)
                                        .setFontSize(18)
                                        .setTextAlignment(TextAlignment.CENTER);
                        document.add(title);

                        Paragraph dateGen = new Paragraph("Generated: " + java.time.LocalDateTime.now())
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setFontSize(10);
                        document.add(dateGen);
                        document.add(new Paragraph("\n"));

                        // Table with summary
                        Table summaryTable = new Table(
                                        UnitValue.createPercentArray(new float[] { 10, 20, 20, 15, 15, 20 }))
                                        .useAllAvailableWidth();
                        summaryTable.addCell(new Cell().add(new Paragraph("ID").setFont(boldFont)));
                        summaryTable.addCell(new Cell().add(new Paragraph("Equipment").setFont(boldFont)));
                        summaryTable.addCell(new Cell().add(new Paragraph("Inspector").setFont(boldFont)));
                        summaryTable.addCell(new Cell().add(new Paragraph("Date").setFont(boldFont)));
                        summaryTable.addCell(new Cell().add(new Paragraph("Status").setFont(boldFont)));
                        summaryTable.addCell(new Cell().add(new Paragraph("Findings").setFont(boldFont)));

                        for (cds.gen.inspectionservice.Inspection insp : inspections) {
                                String equipName = insp.getEquipment() != null ? insp.getEquipment().getName() : "N/A";
                                String inspName = insp.getInspector() != null ? insp.getInspector().getName() : "N/A";
                                String dateStr = insp.getInspectionDate() != null ? insp.getInspectionDate().toString()
                                                : "N/A";
                                String findings = insp.getFindings() != null ? insp.getFindings().substring(0,
                                                Math.min(30, insp.getFindings().length())) : "N/A";

                                summaryTable.addCell(new Cell().add(new Paragraph(insp.getId()).setFont(regularFont)));
                                summaryTable.addCell(new Cell().add(new Paragraph(equipName).setFont(regularFont)));
                                summaryTable.addCell(new Cell().add(new Paragraph(inspName).setFont(regularFont)));
                                summaryTable.addCell(new Cell().add(new Paragraph(dateStr).setFont(regularFont)));
                                summaryTable.addCell(new Cell()
                                                .add(new Paragraph(insp.getStatus() != null ? insp.getStatus() : "N/A")
                                                                .setFont(regularFont)));
                                summaryTable.addCell(
                                                new Cell().add(new Paragraph(findings + "...").setFont(regularFont)));
                        }

                        document.add(summaryTable);
                        document.close();

                        logger.info("Generated batch report for {} inspections", inspections.size());
                        return baos.toByteArray();
                } catch (Exception e) {
                        logger.error("Error generating multiple inspections report", e);
                        throw new RuntimeException("Failed to generate batch report", e);
                }
        }
}