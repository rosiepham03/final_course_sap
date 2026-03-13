namespace equipment.inspection;

using { cuid, managed } from '@sap/cds/common';

/**
 * Equipment entity - represents equipment/machinery being inspected
 */
entity Equipment : cuid {
    name: String;
    type: String;
    location: String;
    serialNumber: String;
    manufacturer: String;
    installationDate: Date;
    status: String enum { Active; Inactive; Maintenance }
}

/**
 * Inspector entity - represents employees performing inspections
 */
entity Inspector : cuid {
    name: String;
    employeeId: String;
    department: String;
    email: String;
    certifications: String;
}

/**
 * Inspection entity - represents individual inspection records
 */
entity Inspection : cuid, managed {
    equipment: Association to Equipment;
    inspector: Association to Inspector;
    inspectionDate: Date;
    completionDate: Date;
    status: String enum { 
        Planned; 
        InProgress; 
        Completed; 
        Failed 
    };
    notes: String;
    findings: String;
    safetyIssues: String;
    nextInspectionDate: Date;
    // One inspection has many inspection reports (composition relationship).
    // We link parent and child via the child's foreign key field `inspection_ID`
    // to avoid referencing a managed association directly in the ON condition.
    inspectionReport: Composition of many InspectionReport
        on inspectionReport.inspection_ID = ID;
}

/**
 * InspectionReport entity - stores the generated report with additional data
 */
entity InspectionReport : cuid, managed {
    // Foreign key + association back to the parent Inspection for the composition above
    inspection_ID   : UUID;
    inspection      : Association to Inspection on inspection.ID = inspection_ID;
    additionalComments: String;
    approvedBy: String;
    approvalDate: Date;
    reportPdf: LargeString; // Base64 encoded PDF
    exportedAt: Timestamp;
}

