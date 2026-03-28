package customer.final_course_sap.report;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

public class PdfReportGenerator {

        private static final Logger logger = LoggerFactory.getLogger(PdfReportGenerator.class);

        /* Typography */
        private static final float TITLE_SIZE = 20f;
        private static final float SECTION_SIZE = 13f;
        private static final float BODY_SIZE = 10f;
        private static final float META_SIZE = 9f;
        private static final float TABLE_HEADER_SIZE = 10f;
        private static final float LEADING = 1.15f;

        /* Layout */
        private static final float CELL_PADDING = 7f;
        private static final float BORDER_WIDTH = 0.5f;

        /* Palette */
        private static final DeviceRgb HEADER_BG = new DeviceRgb(44, 62, 80);
        private static final DeviceRgb HEADER_FG = new DeviceRgb(255, 255, 255);
        private static final DeviceRgb ALT_ROW_BG = new DeviceRgb(247, 248, 250);
        private static final DeviceRgb BORDER_GRAY = new DeviceRgb(210, 214, 220);
        private static final DeviceRgb STATUS_COMPLETED_BG = new DeviceRgb(209, 250, 229);
        private static final DeviceRgb STATUS_COMPLETED_FG = new DeviceRgb(6, 95, 70);
        private static final DeviceRgb STATUS_PROGRESS_BG = new DeviceRgb(254, 243, 199);
        private static final DeviceRgb STATUS_PROGRESS_FG = new DeviceRgb(146, 64, 14);
        private static final DeviceRgb STATUS_FAILED_BG = new DeviceRgb(254, 226, 226);
        private static final DeviceRgb STATUS_FAILED_FG = new DeviceRgb(153, 27, 27);

        private static SolidBorder cellBorder() {
                return new SolidBorder(BORDER_GRAY, BORDER_WIDTH);
        }

        private static String truncateId(String id, int maxLen) {
                if (id == null || id.isEmpty()) {
                        return "N/A";
                }
                return id.length() <= maxLen ? id : id.substring(0, maxLen);
        }

        private static Paragraph wrappedParagraph(String text, PdfFont font, float fontSize) {
                String t = text != null ? text : "N/A";
                return new Paragraph(t)
                                .setFont(font)
                                .setFontSize(fontSize)
                                .setMultipliedLeading(LEADING);
        }

        private static Cell labelCell(String label, PdfFont boldFont, int rowIndex) {
                Color bg = rowIndex % 2 == 0 ? ColorConstants.WHITE : ALT_ROW_BG;
                return new Cell()
                                .add(wrappedParagraph(label, boldFont, BODY_SIZE))
                                .setPadding(CELL_PADDING)
                                .setBackgroundColor(bg)
                                .setBorder(cellBorder())
                                .setVerticalAlignment(VerticalAlignment.TOP);
        }

        private static Cell valueCell(String value, PdfFont regFont, int rowIndex, boolean isStatus,
                        String rawStatus) {
                Color bg = rowIndex % 2 == 0 ? ColorConstants.WHITE : ALT_ROW_BG;
                Paragraph p = wrappedParagraph(value, regFont, BODY_SIZE);
                Cell cell = new Cell()
                                .add(p)
                                .setPadding(CELL_PADDING)
                                .setBackgroundColor(bg)
                                .setBorder(cellBorder())
                                .setVerticalAlignment(VerticalAlignment.TOP);
                if (isStatus) {
                        applyStatusStyle(cell, p, rawStatus);
                }
                return cell;
        }

        private static void applyStatusStyle(Cell cell, Paragraph paragraph, String status) {
                StatusStyle style = resolveStatusStyle(status);
                cell.setBackgroundColor(style.background);
                paragraph.setFontColor(style.foreground);
        }

        private static final class StatusStyle {
                final Color background;
                final Color foreground;

                StatusStyle(Color background, Color foreground) {
                        this.background = background;
                        this.foreground = foreground;
                }
        }

        private static StatusStyle resolveStatusStyle(String status) {
                if (status == null) {
                        return new StatusStyle(ColorConstants.WHITE, ColorConstants.BLACK);
                }
                String s = status.trim();
                if (s.equalsIgnoreCase("Completed")) {
                        return new StatusStyle(STATUS_COMPLETED_BG, STATUS_COMPLETED_FG);
                }
                if (s.equalsIgnoreCase("InProgress")) {
                        return new StatusStyle(STATUS_PROGRESS_BG, STATUS_PROGRESS_FG);
                }
                if (s.equalsIgnoreCase("Failed")) {
                        return new StatusStyle(STATUS_FAILED_BG, STATUS_FAILED_FG);
                }
                return new StatusStyle(ColorConstants.WHITE, ColorConstants.BLACK);
        }

        private static void addKeyValueRow(Table table, String label, String value, PdfFont bold, PdfFont reg,
                        int rowIndex, boolean highlightStatus) {
                String rawStatus = highlightStatus ? value : null;
                table.addCell(labelCell(label, bold, rowIndex));
                table.addCell(valueCell(value, reg, rowIndex, highlightStatus, rawStatus));
        }

        private static void addSectionHeader(Document doc, String title, PdfFont font) {
                doc.add(new Paragraph(title)
                                .setFont(font)
                                .setFontSize(SECTION_SIZE)
                                .setMarginTop(4f)
                                .setMarginBottom(6f));
        }

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

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        PdfWriter writer = new PdfWriter(baos);
                        PdfDocument pdfDoc = new PdfDocument(writer);
                        Document document = new Document(pdfDoc);
                        document.setMargins(40, 40, 40, 40);

                        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

                        document.add(new Paragraph("EQUIPMENT INSPECTION REPORT")
                                        .setFont(boldFont)
                                        .setFontSize(TITLE_SIZE)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setMarginBottom(8f));

                        addSectionHeader(document, "EQUIPMENT INFORMATION", boldFont);
                        Table equipmentTable = new Table(UnitValue.createPercentArray(new float[] { 28, 72 }))
                                        .useAllAvailableWidth();

                        int row = 0;
                        addKeyValueRow(equipmentTable, "Equipment Name:", equipmentName, boldFont, regularFont, row++,
                                        false);
                        addKeyValueRow(equipmentTable, "Equipment Type:", equipmentType, boldFont, regularFont, row++,
                                        false);
                        addKeyValueRow(equipmentTable, "Location:", location, boldFont, regularFont, row++, false);
                        addKeyValueRow(equipmentTable, "Serial Number:", serialNumber, boldFont, regularFont, row++,
                                        false);

                        document.add(equipmentTable);
                        document.add(new Paragraph().setMarginBottom(4f));

                        addSectionHeader(document, "INSPECTION DETAILS", boldFont);
                        Table inspectionTable = new Table(UnitValue.createPercentArray(new float[] { 28, 72 }))
                                        .useAllAvailableWidth();

                        row = 0;
                        addKeyValueRow(inspectionTable, "Inspection Date:", inspectionDate, boldFont, regularFont,
                                        row++, false);
                        addKeyValueRow(inspectionTable, "Completion Date:", completionDate, boldFont, regularFont,
                                        row++, false);
                        addKeyValueRow(inspectionTable, "Status:", status, boldFont, regularFont, row++, true);

                        document.add(inspectionTable);
                        document.add(new Paragraph().setMarginBottom(4f));

                        addSectionHeader(document, "INSPECTION FINDINGS", boldFont);
                        document.add(wrappedParagraph(findings != null ? findings : "No findings documented",
                                        regularFont, BODY_SIZE)
                                        .setMarginBottom(8f));

                        if (notes != null && !notes.isBlank()) {
                                addSectionHeader(document, "NOTES", boldFont);
                                document.add(wrappedParagraph(notes, regularFont, BODY_SIZE).setMarginBottom(8f));
                        }

                        if (additionalComments != null && !additionalComments.isBlank()) {
                                addSectionHeader(document, "ADDITIONAL COMMENTS", boldFont);
                                document.add(wrappedParagraph(additionalComments, regularFont, BODY_SIZE)
                                                .setMarginBottom(8f));
                        }

                        if (safetyIssues != null && !safetyIssues.isEmpty()) {
                                addSectionHeader(document, "SAFETY ISSUES", boldFont);
                                document.add(wrappedParagraph(safetyIssues, regularFont, BODY_SIZE)
                                                .setMarginBottom(8f));
                        }

                        addSectionHeader(document, "APPROVAL", boldFont);
                        Table approvalTable = new Table(UnitValue.createPercentArray(new float[] { 28, 72 }))
                                        .useAllAvailableWidth();

                        row = 0;
                        addKeyValueRow(approvalTable, "Approved By:", approvedBy != null ? approvedBy : "Pending",
                                        boldFont, regularFont, row++, false);
                        addKeyValueRow(approvalTable, "Approval Date:", approvalDate != null ? approvalDate : "Pending",
                                        boldFont, regularFont, row++, false);

                        document.add(approvalTable);

                        document.close();

                        logger.info("PDF report generated successfully for: {}", equipmentName);
                        return baos.toByteArray();

                } catch (Exception e) {
                        logger.error("Error generating PDF report", e);
                        throw new RuntimeException("Failed to generate PDF report", e);
                }
        }

        private static Cell summaryHeaderCell(String text, PdfFont boldFont) {
                return new Cell()
                                .add(new Paragraph(text)
                                                .setFont(boldFont)
                                                .setFontSize(TABLE_HEADER_SIZE)
                                                .setFontColor(HEADER_FG)
                                                .setMultipliedLeading(LEADING))
                                .setPadding(CELL_PADDING)
                                .setBackgroundColor(HEADER_BG)
                                .setBorder(cellBorder())
                                .setTextAlignment(TextAlignment.LEFT)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        }

        private static Cell summaryDataCell(Paragraph content, boolean alternate) {
                Color bg = alternate ? ALT_ROW_BG : ColorConstants.WHITE;
                return new Cell()
                                .add(content)
                                .setPadding(CELL_PADDING)
                                .setBackgroundColor(bg)
                                .setBorder(cellBorder())
                                .setVerticalAlignment(VerticalAlignment.TOP);
        }

        private static Paragraph statusParagraph(String statusText, PdfFont font) {
                StatusStyle st = resolveStatusStyle(statusText);
                String display = statusText != null ? statusText : "N/A";
                return new Paragraph(display)
                                .setFont(font)
                                .setFontSize(BODY_SIZE)
                                .setFontColor(st.foreground)
                                .setMultipliedLeading(LEADING);
        }

        private static Cell statusDataCell(String statusText, PdfFont font) {
                StatusStyle st = resolveStatusStyle(statusText);
                return new Cell()
                                .add(statusParagraph(statusText, font))
                                .setPadding(CELL_PADDING)
                                .setBackgroundColor(st.background)
                                .setBorder(cellBorder())
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        }

        public static byte[] generateMultipleInspectionsReport(
                        List<cds.gen.inspectionservice.Inspection> inspections) {
                List<cds.gen.inspectionservice.Inspection> rows = inspections == null ? Collections.emptyList()
                                : inspections;
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        PdfWriter writer = new PdfWriter(baos);
                        PdfDocument pdfDoc = new PdfDocument(writer);
                        Document document = new Document(pdfDoc);
                        document.setMargins(36, 36, 36, 36);

                        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

                        document.add(new Paragraph("EQUIPMENT INSPECTION REPORT — BATCH")
                                        .setFont(boldFont)
                                        .setFontSize(18f)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setMarginBottom(4f));

                        document.add(new Paragraph("Generated: " + java.time.LocalDateTime.now())
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setFont(regularFont)
                                        .setFontSize(META_SIZE)
                                        .setFontColor(new DeviceRgb(90, 90, 90))
                                        .setMarginBottom(12f));

                        float[] cols = new float[] { 11f, 18f, 18f, 14f, 14f, 25f };
                        Table summaryTable = new Table(UnitValue.createPercentArray(cols)).useAllAvailableWidth();

                        summaryTable.addCell(summaryHeaderCell("ID", boldFont));
                        summaryTable.addCell(summaryHeaderCell("Equipment", boldFont));
                        summaryTable.addCell(summaryHeaderCell("Inspector", boldFont));
                        summaryTable.addCell(summaryHeaderCell("Date", boldFont));
                        summaryTable.addCell(summaryHeaderCell("Status", boldFont));
                        summaryTable.addCell(summaryHeaderCell("Findings", boldFont));

                        if (rows.isEmpty()) {
                                summaryTable.addCell(new Cell(1, 6)
                                                .add(wrappedParagraph(
                                                                "No inspections matched the requested IDs (or no IDs were provided).",
                                                                regularFont, BODY_SIZE))
                                                .setPadding(CELL_PADDING)
                                                .setBackgroundColor(ColorConstants.WHITE)
                                                .setBorder(cellBorder())
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE));
                        }

                        int i = 0;
                        for (cds.gen.inspectionservice.Inspection insp : rows) {
                                boolean alternate = i % 2 == 1;
                                String idFull = insp.getId();
                                String idShort = truncateId(idFull, 8);

                                String equipName = insp.getEquipment() != null ? insp.getEquipment().getName() : "N/A";
                                String inspName = insp.getInspector() != null ? insp.getInspector().getName() : "N/A";
                                String dateStr = insp.getInspectionDate() != null
                                                ? insp.getInspectionDate().toString()
                                                : "N/A";
                                String findingsText = insp.getFindings() != null ? insp.getFindings() : "N/A";
                                String statusVal = insp.getStatus();

                                summaryTable.addCell(summaryDataCell(
                                                wrappedParagraph(idShort, regularFont, BODY_SIZE), alternate));
                                summaryTable.addCell(summaryDataCell(
                                                wrappedParagraph(equipName, regularFont, BODY_SIZE), alternate));
                                summaryTable.addCell(summaryDataCell(
                                                wrappedParagraph(inspName, regularFont, BODY_SIZE), alternate));
                                summaryTable.addCell(summaryDataCell(
                                                wrappedParagraph(dateStr, regularFont, BODY_SIZE), alternate));
                                summaryTable.addCell(statusDataCell(statusVal, regularFont));
                                summaryTable.addCell(summaryDataCell(
                                                wrappedParagraph(findingsText, regularFont, BODY_SIZE), alternate));
                                i++;
                        }

                        document.add(summaryTable);
                        document.close();

                        logger.info("Generated batch report for {} inspections", rows.size());
                        return baos.toByteArray();
                } catch (Exception e) {
                        logger.error("Error generating multiple inspections report", e);
                        throw new RuntimeException("Failed to generate batch report", e);
                }
        }
}
