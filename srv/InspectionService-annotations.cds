using InspectionService from './InspectionService';

annotate InspectionService.Equipment with {
    ID @UI.Hidden;
    name @Common.Label: 'Equipment Name';
    type @Common.Label: 'Equipment Type';
    location @Common.Label: 'Location';
    serialNumber @Common.Label: 'Serial Number';
    manufacturer @Common.Label: 'Manufacturer';
    installationDate @Common.Label: 'Installation Date';
    status @Common.Label: 'Status';
};

annotate InspectionService.Inspector with {
    ID @UI.Hidden;
    name @Common.Label: 'Inspector Name';
    employeeId @Common.Label: 'Employee ID';
    department @Common.Label: 'Department';
    email @Common.Label: 'Email';
    certifications @Common.Label: 'Certifications';
};

annotate InspectionService.Inspection with {
    ID @UI.Hidden;
    equipment @Common.Label: 'Equipment';
    inspector @Common.Label: 'Inspector';
    inspectionDate @Common.Label: 'Inspection Date';
    completionDate @Common.Label: 'Completion Date';
    status @Common.Label: 'Status';
    notes @Common.Label: 'Notes';
    findings @Common.Label: 'Findings';
    safetyIssues @Common.Label: 'Safety Issues';
    nextInspectionDate @Common.Label: 'Next Inspection Date';
};

annotate InspectionService.InspectionReport with {
    ID @UI.Hidden;
    inspection @Common.Label: 'Inspection';
    additionalComments @Common.Label: 'Additional Comments';
    approvedBy @Common.Label: 'Approved By';
    approvalDate @Common.Label: 'Approval Date';
    reportPdf @UI.Hidden;
};

annotate InspectionService.Inspection with @(
    UI.LineItem: [
        { Value: equipment.name, Label: 'Equipment' },
        { Value: inspector.name, Label: 'Inspector' },
        { Value: inspectionDate, Label: 'Inspection Date' },
        { Value: status, Label: 'Status' },
        { Value: findings, Label: 'Findings' }
    ],
    UI.HeaderInfo: {
        TypeName: 'Inspection',
        TypeNamePlural: 'Inspections',
        Title: { Value: equipment.name },
        Description: { Value: status }
    },
    UI.Facets: [
        {
            $Type: 'UI.ReferenceFacet',
            Label: 'Inspection Information',
            Target: '@UI.FieldGroup#GeneralInformation'
        },
        {
            $Type: 'UI.ReferenceFacet',
            Label: 'Findings',
            Target: '@UI.FieldGroup#Findings'
        }
    ],
    UI.FieldGroup#GeneralInformation: {
        Data: [
            { Value: equipment.name, Label: 'Equipment' },
            { Value: inspector.name, Label: 'Inspector' },
            { Value: inspectionDate, Label: 'Inspection Date' },
            { Value: completionDate, Label: 'Completion Date' },
            { Value: status, Label: 'Status' }
        ]
    },
    UI.FieldGroup#Findings: {
        Data: [
            { Value: findings, Label: 'Findings' },
            { Value: safetyIssues, Label: 'Safety Issues' },
            { Value: notes, Label: 'Notes' }
        ]
    }
);
