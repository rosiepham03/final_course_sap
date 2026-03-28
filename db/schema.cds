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
    status: String enum { Planned; InProgress; Completed; Failed };
    
    notes: String;
    findings: String;
    safetyIssues: String;
    nextInspectionDate: Date;

    // Composition đúng cách
    inspectionReports: Composition of many InspectionReport on inspectionReports.inspection = $self;
}

entity InspectionReport : cuid, managed {
    inspection: Association to Inspection;
    
    additionalComments: String;
    approvedBy: Association to Inspector;   // tốt hơn là String
    approvalDate: Date;
    
    reportPdf: LargeBinary;                 // thay vì LargeString
    exportedAt: Timestamp;
}

entity InspectionComment {
  key ID : UUID;
  inspection : Association to Inspection;
  commentText : String;
  createdBy : String;
  createdAt : Timestamp;
}

entity InspectionApproval {
  key ID : UUID;
  inspection : Association to Inspection;
  approvedBy : String;
  approvalDate : Timestamp;
  note : String;
}