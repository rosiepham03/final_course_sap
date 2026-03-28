using {equipment.inspection as inspection} from '../db/schema';

/**
 * Inspection Service - OData V4 service for Equipment Inspection
 */
service InspectionService {

    type EquipmentData {
        ID           : String;
        name         : String;
        type         : String;
        location     : String;
        serialNumber : String;
        status       : String;
    }

    type InspectionSearchResult {
        ID             : String;
        equipmentName  : String;
        equipmentType  : String;
        inspectionDate : Date;
        inspectorName  : String;
        status         : String;
        findings       : String;
    }

    type ReportGenerationRequest {
        inspectionId       : String;
        additionalComments : String;
    }

    type ReportGenerationResponse {
        reportId : String;
        pdfUrl   : String;
        message  : String;
    }

    entity Equipment        as projection on inspection.Equipment;
    entity Inspector        as projection on inspection.Inspector;
    entity Inspection       as projection on inspection.Inspection;
    entity InspectionReport as projection on inspection.InspectionReport;

    action   addComment(inspectionId: String, commentText: String)            returns Boolean;

    action   approveInspection(inspectionId: String,
                               additionalComments: String,
                               approvalNote: String)                          returns Boolean;

    action   generateReport(inspectionId: String, additionalComments: String) returns LargeBinary @Core.ContentDisposition: {
        Filename: 'Inspection_Report.pdf',
        Type    : 'attachment'
    };

    function exportReportPdf(reportId: String)                                returns LargeBinary;

    function searchInspections(
        search: String,
        status: String,
        equipmentId: String,
        inspectorId: String,
        inspectionDateFrom: Date,
        inspectionDateTo: Date,
        equipmentName: String,
        limit: Integer,
        offset: Integer
    ) returns array of InspectionSearchResult;

    /// Get inspection details with equipment and inspector info
    function getInspectionDetails(inspectionId: String)                       returns {
        inspectionId        : String;
        equipmentName       : String;
        equipmentType       : String;
        equipmentLocation   : String;
        serialNumber        : String;
        inspectionDate      : Date;
        completionDate      : Date;
        inspectorName       : String;
        inspectorDepartment : String;
        status              : String;
        findings            : String;
        safetyIssues        : String;
        notes               : String;
    };

    action printInspections(inspectionIds: array of String) returns LargeBinary @Core.ContentDisposition: {
        Filename: 'Inspections_Report.pdf',
        Type    : 'attachment'
    };
}
