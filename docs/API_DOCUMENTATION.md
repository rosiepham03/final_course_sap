# Equipment Inspection Report - API Documentation

Complete API reference for the Equipment Inspection Report system.

---

## Base URL

```
http://localhost:8080/api
```

---

## Authentication

Currently, the API is open (no authentication required). In production, add Spring Security:

```yaml
# application.yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-auth-server
```

---

## Response Format

### Success Response (200 OK)
```json
{
  "data": [/* array or object */],
  "status": "success",
  "timestamp": "2026-03-13T10:30:00Z"
}
```

### Error Response (4xx, 5xx)
```json
{
  "error": "Error message",
  "status": "error",
  "code": 500,
  "timestamp": "2026-03-13T10:30:00Z"
}
```

---

## Endpoints

### 1. EQUIPMENT ENDPOINTS

#### 1.1 Get All Equipment
```http
GET /api/equipment
```

**Parameters:** None

**Response (200):**
```json
[
  {
    "ID": "8f8f1234-5678-9abc-def0-123456789abc",
    "name": "Hydraulic Press A1",
    "type": "Hydraulic Press",
    "location": "Building A - Floor 1",
    "serialNumber": "HYD-2019-0001",
    "manufacturer": "Hydraulic Systems Inc",
    "installationDate": "2019-03-15",
    "status": "Active"
  },
  {
    "ID": "8f8f2345-6789-abcd-ef01-234567890abc",
    "name": "CNC Machine B2",
    "type": "CNC Machine",
    "location": "Building B - Floor 2",
    "serialNumber": "CNC-2020-0002",
    "manufacturer": "Precision Engineering Ltd",
    "installationDate": "2020-06-20",
    "status": "Active"
  }
]
```

**Error (500):**
```json
{
  "error": "Failed to fetch equipment: Database connection error",
  "status": "error"
}
```

---

### 2. INSPECTOR ENDPOINTS

#### 2.1 Get All Inspectors
```http
GET /api/inspectors
```

**Parameters:** None

**Response (200):**
```json
[
  {
    "ID": "1a1a1111-2222-3333-4444-555555555aaa",
    "name": "John Smith",
    "employeeId": "EMP001",
    "department": "Manufacturing",
    "email": "john.smith@company.com",
    "certifications": "ISO9001,OHSAS18001"
  },
  {
    "ID": "2b2b2222-3333-4444-5555-666666666bbb",
    "name": "Sarah Johnson",
    "employeeId": "EMP002",
    "department": "Quality Assurance",
    "email": "sarah.johnson@company.com",
    "certifications": "ISO9001,SQF"
  }
]
```

---

### 3. INSPECTION ENDPOINTS

#### 3.1 Get All Inspections
```http
GET /api/inspections
```

**Parameters:** None

**Response (200):**
```json
[
  {
    "ID": "5f5f1111-6666-7777-8888-999999999fff",
    "equipment_ID": "8f8f1234-5678-9abc-def0-123456789abc",
    "inspector_ID": "1a1a1111-2222-3333-4444-555555555aaa",
    "inspectionDate": "2025-11-15",
    "completionDate": "2025-11-15",
    "status": "Completed",
    "notes": "Routine quarterly inspection",
    "findings": "All components within normal parameters",
    "safetyIssues": null,
    "nextInspectionDate": "2026-02-15"
  }
]
```

---

#### 3.2 Get Inspection by ID
```http
GET /api/inspections/{inspectionId}
```

**Parameters:**
- `inspectionId` (Path, required): UUID of the inspection

**Example:**
```http
GET /api/inspections/5f5f1111-6666-7777-8888-999999999fff
```

**Response (200):**
```json
{
  "ID": "5f5f1111-6666-7777-8888-999999999fff",
  "equipment_ID": "8f8f1234-5678-9abc-def0-123456789abc",
  "inspector_ID": "1a1a1111-2222-3333-4444-555555555aaa",
  "inspectionDate": "2025-11-15",
  "completionDate": "2025-11-15",
  "status": "Completed",
  "notes": "Routine quarterly inspection",
  "findings": "All components within normal parameters",
  "safetyIssues": null,
  "nextInspectionDate": "2026-02-15"
}
```

**Error (404):**
```json
{
  "error": "Not Found",
  "status": "error"
}
```

---

#### 3.3 Search Inspections (Advanced Filtering)
```http
GET /api/search
```

**Query Parameters:**
- `equipmentId` (Optional): Equipment UUID to filter by
- `inspectorId` (Optional): Inspector UUID to filter by
- `status` (Optional): Status filter (Planned, InProgress, Completed, Failed)

**Examples:**

Filter by Status:
```http
GET /api/search?status=Completed
```

Filter by Equipment:
```http
GET /api/search?equipmentId=8f8f1234-5678-9abc-def0-123456789abc
```

Filter by Inspector:
```http
GET /api/search?inspectorId=1a1a1111-2222-3333-4444-555555555aaa
```

Multiple Filters:
```http
GET /api/search?status=Completed&equipmentId=8f8f1234-5678-9abc-def0-123456789abc&inspectorId=1a1a1111-2222-3333-4444-555555555aaa
```

**Response (200):**
```json
[
  {
    "ID": "5f5f1111-6666-7777-8888-999999999fff",
    "equipment_ID": "8f8f1234-5678-9abc-def0-123456789abc",
    "inspector_ID": "1a1a1111-2222-3333-4444-555555555aaa",
    "inspectionDate": "2025-11-15",
    "completionDate": "2025-11-15",
    "status": "Completed",
    "findings": "All components within normal parameters"
  }
]
```

---

### 4. REPORT ENDPOINTS

#### 4.1 Generate Inspection Report
```http
POST /api/reports/generate
```

**Query Parameters:**
- `inspectionId` (Required): UUID of the inspection
- `additionalComments` (Optional): Additional comments to include in report

**Examples:**

Basic:
```http
POST /api/reports/generate?inspectionId=5f5f1111-6666-7777-8888-999999999fff
```

With Comments:
```http
POST /api/reports/generate?inspectionId=5f5f1111-6666-7777-8888-999999999fff&additionalComments=Approved%20for%20production%20use
```

**Response (200):**
```json
{
  "inspectionId": "5f5f1111-6666-7777-8888-999999999fff",
  "message": "Report generation initiated",
  "equipment": "Hydraulic Press A1",
  "inspector": "John Smith",
  "status": "Completed"
}
```

**Error (404):**
```json
{
  "error": "Inspection not found: 5f5f1111-6666-7777-8888-999999999fff",
  "status": "error"
}
```

**Error (500):**
```json
{
  "error": "Failed to generate report: PDF generation error",
  "status": "error"
}
```

---

#### 4.2 Export Report as PDF
```http
GET /api/reports/export/{reportId}
```

**Parameters:**
- `reportId` (Path, required): UUID of the report

**Example:**
```http
GET /api/reports/export/report-uuid-here
```

**Response (200):**
Binary PDF file with header:
```
Content-Type: application/pdf
Content-Disposition: attachment; filename="inspection_report_report-uuid-here.pdf"
```

**Error (404):**
```json
{
  "error": "Report not found: report-uuid-here",
  "status": "error"
}
```

---

## Status Codes

| Code | Meaning |
|------|---------|
| **200** | OK - Request successful |
| **201** | Created - Resource created |
| **204** | No Content - Successful but no content |
| **400** | Bad Request - Invalid parameters |
| **401** | Unauthorized - Authentication required |
| **403** | Forbidden - Access denied |
| **404** | Not Found - Resource not found |
| **409** | Conflict - Resource conflict |
| **500** | Internal Server Error |
| **503** | Service Unavailable |

---

## Common Errors

### Error: Inspection Not Found
```json
{
  "error": "Inspection not found: invalid-id",
  "status": "error"
}
```

**Solution:** Verify inspection ID exists. Use `GET /api/inspections` to list valid IDs.

---

### Error: Database Connection Failed
```json
{
  "error": "Failed to search inspections: Cannot get a connection",
  "status": "error"
}
```

**Solution:** 
1. Ensure H2 database is running
2. Check `application.yaml` datasource configuration
3. Restart the application

---

### Error: PDF Generation Failed
```json
{
  "error": "Failed to generate report: PDF generation error",
  "status": "error"
}
```

**Solution:**
1. Verify iText library is installed: `mvn dependency:tree | grep itext`
2. Check application logs for detailed error
3. Ensure inspection data is complete (no null critical fields)

---

## Usage Examples

### Example 1: Complete Workflow

**Step 1:** List all inspections
```bash
curl http://localhost:8080/api/inspections
```

**Step 2:** Find inspection to report on (note the ID)
```bash
curl "http://localhost:8080/api/search?status=Completed"
```

**Step 3:** Generate report with comments
```bash
curl -X POST "http://localhost:8080/api/reports/generate?inspectionId=5f5f1111-6666-7777-8888-999999999fff&additionalComments=All+systems+operational"
```

**Step 4:** Extract report ID from response and export PDF
```bash
curl "http://localhost:8080/api/reports/export/{reportId}" -o inspection_report.pdf
```

---

### Example 2: PowerShell Script

```powershell
# Get all completed inspections
$inspections = Invoke-RestMethod -Uri "http://localhost:8080/api/search?status=Completed" -Method Get

# Generate report for first inspection
$reportResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/reports/generate?inspectionId=$($inspections[0].ID)&additionalComments=Approved" -Method Post

Write-Host "Report ID: $($reportResponse.reportId)"
Write-Host "Report URL: $($reportResponse.pdfUrl)"

# Download PDF
Invoke-WebRequest -Uri "http://localhost:8080/api/reports/export/$($reportResponse.reportId)" -OutFile "inspection_report.pdf"
```

---

## Rate Limiting

Currently, no rate limiting is implemented. For production, add:

```yaml
# application.yaml
spring:
  cloud:
    gateway:
      routes:
      - id: inspection-api
        uri: http://localhost:8080
        predicates:
        - Path=/api/**
        filters:
        - name: RequestRateLimiter
          args:
            redis-rate-limiter:
              replenish-rate: 100
              burst-capacity: 200
```

---

## Pagination

Add pagination support to list endpoints by modifying controller:

```java
@GetMapping("/inspections")
public ResponseEntity<?> getAllInspections(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    // Implementation
}
```

Query:
```http
GET /api/inspections?page=0&size=10
```

---

## Sorting

Add sorting support:

```http
GET /api/inspections?sort=inspectionDate,desc
GET /api/search?status=Completed&sort=equipment.name,asc
```

---

## Caching

Add response caching:

```java
@GetMapping("/equipment")
@Cacheable(value = "equipment", ttl = 3600)
public ResponseEntity<?> getAllEquipment() {
    // Implementation
}
```

---

## Documentation Updates

This documentation reflects **API Version 1.0.0**

Last Updated: March 13, 2026

---

