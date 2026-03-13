using { equipment.inspection } from '../db/schema';

/**
 * Inspection Service - OData V4 service for Equipment Inspection
 */
service InspectionService {

    type EquipmentData {
        ID: String;
        name: String;
        type: String;
        location: String;
        serialNumber: String;
        status: String;
    }

    type InspectionSearchResult {
        ID: String;
        equipmentName: String;
        equipmentType: String;
        inspectionDate: Date;
        inspectorName: String;
        status: String;
        findings: String;
    }

    type ReportGenerationRequest {
        inspectionId: String;
        additionalComments: String;
    }

    type ReportGenerationResponse {
        reportId: String;
        pdfUrl: String;
        message: String;
    }

    entity Equipment as projection on inspection.Equipment;
    entity Inspector as projection on inspection.Inspector;
    entity Inspection as projection on inspection.Inspection;
    entity InspectionReport as projection on inspection.InspectionReport;

    /// Search inspections with filtering
    function searchInspections(
        equipmentId: String,
        equipmentName: String,
        inspectionDateFrom: Date,
        inspectionDateTo: Date,
        inspectorId: String,
        status: String
    ) returns array of InspectionSearchResult;

    /// Generate inspection report
    function generateReport(
        inspectionId: String,
        additionalComments: String
    ) returns ReportGenerationResponse;

    /// Export report as PDF (returns Base64 encoded PDF)
    function exportReportPdf(reportId: String) returns String;

    /// Get inspection details with equipment and inspector info
    function getInspectionDetails(inspectionId: String) returns {
        inspectionId: String;
        equipmentName: String;
        equipmentType: String;
        equipmentLocation: String;
        serialNumber: String;
        inspectionDate: Date;
        completionDate: Date;
        inspectorName: String;
        inspectorDepartment: String;
        status: String;
        findings: String;
        safetyIssues: String;
        notes: String;
    };
}
