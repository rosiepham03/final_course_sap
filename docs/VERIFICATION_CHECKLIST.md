# Equipment Inspection Report - Verification Checklist

Use this checklist to verify the project is set up correctly and all components are working.

---

## ✅ Pre-Installation Checklist

### System Requirements
- [ ] Windows/Mac/Linux OS installed
- [ ] Internet connection available
- [ ] At least 4GB free disk space
- [ ] Administrator/sudo access available if needed

### Required Software
- [ ] Java 21 JDK installed (`java -version` shows version 21.x)
- [ ] Maven 3.9+ installed (`mvn -version` shows 3.9+)
- [ ] Node.js 18+ installed (`node -v` shows version 18+)
- [ ] npm installed (`npm -v` shows version)
- [ ] Text editor or IDE available (VS Code, IntelliJ, etc.)
- [ ] Git installed (optional but recommended)

### Environment Setup
- [ ] JAVA_HOME environment variable set correctly
- [ ] M2_HOME environment variable set (for Maven)
- [ ] PATH includes Java and Maven bin directories
- [ ] PATH includes Node.js bin directory

---

## ✅ Installation Verification

### Step 1: Project Structure
After cloning/extracting the project, verify:
- [ ] `db/schema.cds` exists
- [ ] `db/data/equipment.csv` exists
- [ ] `db/data/inspector.csv` exists
- [ ] `db/data/inspection.csv` exists
- [ ] `srv/InspectionService.cds` exists
- [ ] `srv/pom.xml` exists
- [ ] `srv/src/main/java/...` directories exist
- [ ] `pom.xml` (root) exists
- [ ] `package.json` exists
- [ ] README.md exists

### Step 2: Dependencies Installation
```bash
# ✅ Verify Node dependencies installed
npm install
# Should complete without errors
# Check: node_modules folder created
- [ ] node_modules/ folder exists
- [ ] @sap/cds-dk installed
- [ ] No npm error messages
```

```bash
# ✅ Verify Maven dependencies
mvn clean install
# Should complete with BUILD SUCCESS
- [ ] "BUILD SUCCESS" in console output
- [ ] cds.gen directory created
- [ ] target/ directories created
```

### Step 3: Code Generation
```bash
mvn cds:generate
# Should generate Java POJOs

- [ ] cds.gen package created in src/main/java
- [ ] InspectionItems.java exists
- [ ] EquipmentItems.java exists
- [ ] InspectorItems.java exists
- [ ] InspectionReportItems.java exists
- [ ] Service context classes generated
```

---

## ✅ Application Startup Verification

### Step 1: Start Application
```bash
cd srv
mvn spring-boot:run
```

**Expected Output (in console):**
```
[main] INFO org.springframework.context.support.AbstractApplicationContext - 
  Refreshing org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext...

[main] INFO org.springframework.boot.web.embedded.tomcat.TomcatWebServer - 
  Tomcat started on port(s): 8080 (http)

[main] INFO customer.final_course_sap.Application - Started Application in XX.XXX seconds
```

**Verification Checklist:**
- [ ] No error messages in console
- [ ] Application started message appears
- [ ] "Tomcat started on port(s): 8080" shown
- [ ] No "Failed to start" message
- [ ] Console accepts commands (not hanging)

### Step 2: Browser Verification
- [ ] Open browser
- [ ] Navigate to `http://localhost:8080`
- [ ] Should see Tomcat/Spring default page or API response (not error)
- [ ] No "Connection refused" error

### Step 3: H2 Console Access
- [ ] Open browser
- [ ] Navigate to `http://localhost:8080/h2-console`
- [ ] H2 login page appears
- [ ] JDBC URL field shows: `jdbc:h2:mem:testdb`
- [ ] Username field shows: `sa`
- [ ] Password field is empty
- [ ] Click "Connect"
- [ ] Console dashboard appears with database structure

### Step 4: Sample Data Verification
In H2 Console, run these queries:

```sql
-- Check Equipment table
SELECT COUNT(*) FROM EQUIPMENT;
-- Should return: 5 rows

SELECT NAME FROM EQUIPMENT;
-- Should show:
-- Hydraulic Press A1
-- CNC Machine B2
-- Welding Robot C3
-- Compressor Unit D4
-- Power Generator E5
```

```sql
-- Check Inspector table
SELECT COUNT(*) FROM INSPECTOR;
-- Should return: 5 rows

SELECT NAME FROM INSPECTOR;
-- Should show inspector names
```

```sql
-- Check Inspection table
SELECT COUNT(*) FROM INSPECTION;
-- Should return: 5 rows

SELECT STATUS, COUNT(*) FROM INSPECTION GROUP BY STATUS;
-- Should show status distribution
```

**Verification Checklist:**
- [ ] EQUIPMENT table has 5 records
- [ ] INSPECTOR table has 5 records
- [ ] INSPECTION table has 5 records
- [ ] All tables have data
- [ ] No SQL errors

---

## ✅ API Endpoint Verification

### Using PowerShell (Windows)

#### Test 1: Get All Equipment
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/equipment" -Method Get
$response | ConvertTo-Json | Write-Host
```

**Verification:**
- [ ] Returns 200 status code
- [ ] Response is JSON array
- [ ] Array has 5 equipment records
- [ ] Each record has: ID, name, type, location, status, etc.

**Expected output:** Array of equipment with their details

---

#### Test 2: Get All Inspections
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/inspections" -Method Get
$response | ConvertTo-Json | Write-Host
```

**Verification:**
- [ ] Returns 200 status code
- [ ] Response is JSON array
- [ ] Array has 5 inspection records
- [ ] Each record has: ID, equipment_ID, inspector_ID, inspectionDate, status, etc.

---

#### Test 3: Get All Inspectors
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/inspectors" -Method Get
$response | ConvertTo-Json | Write-Host
```

**Verification:**
- [ ] Returns 200 status code
- [ ] Response is JSON array
- [ ] Array has 5 inspector records
- [ ] Each record has: ID, name, employeeId, department, email

---

#### Test 4: Search - Completed Inspections
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/search?status=Completed" -Method Get
$response | ConvertTo-Json | Write-Host
```

**Verification:**
- [ ] Returns 200 status code
- [ ] Response is JSON array
- [ ] All records have status = "Completed"
- [ ] Multiple records returned

---

#### Test 5: Get Inspection by ID
```powershell
$inspectionId = "5f5f1111-6666-7777-8888-999999999fff"
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/inspections/$inspectionId" -Method Get
$response | ConvertTo-Json | Write-Host
```

**Verification:**
- [ ] Returns 200 status code
- [ ] Response is single inspection object
- [ ] Returned ID matches requested ID

---

#### Test 6: Generate Report
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/reports/generate?inspectionId=5f5f1111-6666-7777-8888-999999999fff&additionalComments=Approved" -Method Post
$response | ConvertTo-Json | Write-Host
```

**Verification:**
- [ ] Returns 200 status code
- [ ] Response contains "reportId"
- [ ] Response contains "pdfUrl"
- [ ] Response contains "message": "Report generation initiated"
- [ ] Response shows equipment and inspector names

---

#### Test 7: Advanced Search with Multiple Filters
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/search?status=Completed&equipmentId=8f8f1234-5678-9abc-def0-123456789abc" -Method Get
$response | ConvertTo-Json | Write-Host
```

**Verification:**
- [ ] Returns 200 status code
- [ ] Filters are applied correctly
- [ ] All returned records match both criteria

---

## ✅ Database Functionality Verification

### Test Database Connections
In H2 Console:

```sql
-- Test INSERT
INSERT INTO EQUIPMENT (ID, NAME, TYPE, LOCATION, SERIALNUMBER, MANUFACTURER, INSTALLATIONDATE, STATUS) 
VALUES ('test-uuid-1', 'Test Equipment', 'Test Type', 'Test Location', 'TEST-001', 'Test Mfg', '2025-01-01', 'Active');

-- Verify INSERT
SELECT * FROM EQUIPMENT WHERE ID = 'test-uuid-1';
-- Should return the inserted row
```

**Verification:**
- [ ] INSERT command executes without errors
- [ ] SELECT returns the inserted row
- [ ] DELETE removes the test row

```sql
-- Cleanup
DELETE FROM EQUIPMENT WHERE ID = 'test-uuid-1';
```

- [ ] Cleanup successful

---

## ✅ Code Quality Verification

### Check for Compilation Errors
```bash
mvn clean compile
```

**Verification:**
- [ ] No compilation errors
- [ ] "BUILD SUCCESS" message shown
- [ ] target/classes contains compiled .class files

---

### Check for Test Failures
```bash
mvn test
```

**Verification:**
- [ ] Tests run (if any exist)
- [ ] No test failures
- [ ] Coverage metrics shown (if configured)

---

### Check for Dependency Issues
```bash
mvn dependency:tree | findstr /I "itext spring cds"
```

**Verification:**
- [ ] iText 7.2.5 appears in dependency tree
- [ ] Spring Boot 3.5.11 appears
- [ ] CDS services 4.8.0 appears
- [ ] No version conflicts

---

## ✅ PDF Report Generation Verification

### Generate and Export a PDF Report

**Step 1:** Generate Report (via API)
```powershell
$reportResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/reports/generate?inspectionId=5f5f1111-6666-7777-8888-999999999fff&additionalComments=QA+Approved" -Method Post
$reportId = $reportResponse.reportId
Write-Host "Report ID: $reportId"
```

**Verification:**
- [ ] reportId returned
- [ ] reportId is a valid UUID

**Step 2:** Export PDF
```powershell
$reportId = "actual-report-id-from-previous-step"
Invoke-WebRequest -Uri "http://localhost:8080/api/reports/export/$reportId" -OutFile "inspection_report.pdf"
```

**Verification:**
- [ ] Command completes without error
- [ ] File inspection_report.pdf created
- [ ] File size > 0 bytes (contains data)

**Step 3:** Verify PDF Contents
- [ ] Open inspection_report.pdf with PDF viewer
- [ ] Header shows "EQUIPMENT INSPECTION REPORT"
- [ ] Equipment information section visible
- [ ] Inspection details section visible
- [ ] Inspector information section visible
- [ ] Findings section visible
- [ ] PDF is readable (not corrupted)

---

## ✅ Configuration Verification

### Verify application.yaml Settings
Open `srv/src/main/resources/application.yaml`:

- [ ] `spring.datasource.url` contains `jdbc:h2:mem:testdb`
- [ ] `spring.jpa.database-platform` is `org.hibernate.dialect.H2Dialect`
- [ ] `server.port` is `8080`
- [ ] `logging.level.root` includes appropriate log levels

---

## ✅ Documentation Verification

### Verify Documentation Files Exist
- [ ] README.md exists and is > 5KB
- [ ] QUICKSTART.md exists
- [ ] API_DOCUMENTATION.md exists
- [ ] DEVELOPMENT_GUIDE.md exists
- [ ] PROJECT_SUMMARY.md exists

### Verify Documentation Quality
- [ ] README.md has troubleshooting section
- [ ] QUICKSTART.md has working commands
- [ ] API_DOCUMENTATION.md has example requests
- [ ] DEVELOPMENT_GUIDE.md has code examples
- [ ] All files have table of contents or navigation

---

## ✅ Additional Verification

### Check Application Properties
In application console, verify:
- [ ] Server started on localhost:8080
- [ ] No errors in startup logs
- [ ] Spring components autowired successfully
- [ ] CDS services initialized
- [ ] Database connection pool initialized

### Verify File Permissions
- [ ] Can read all Java source files
- [ ] Can write to target/directory
- [ ] Can write to logs/ directory (if logging to file)

### Verify Network
- [ ] Localhost:8080 accessible
- [ ] No firewall blocking port 8080
- [ ] Can write to disk for PDF export

---

## ✅ Performance Verification

### Measure Response Times
```powershell
# Measure GET all inspections
$start = Get-Date
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/inspections" -Method Get
$end = Get-Date
$duration = ($end - $start).TotalMilliseconds
Write-Host "Response time: $duration ms"
```

**Verification:**
- [ ] Response time < 1000ms (1 second)
- [ ] Ideally < 200ms for local testing

```powershell
# Measure PDF generation
$start = Get-Date
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/reports/generate?inspectionId=5f5f1111-6666-7777-8888-999999999fff" -Method Post
$end = Get-Date
$duration = ($end - $start).TotalMilliseconds
Write-Host "Report generation time: $duration ms"
```

**Verification:**
- [ ] PDF generation < 2000ms
- [ ] Ideally < 500ms for local testing

---

## ✅ Error Handling Verification

### Test Invalid Input
```powershell
# Test with non-existent inspection ID
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/inspections/invalid-uuid" -Method Get -ErrorAction SilentlyContinue
# Should return 404 error

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/reports/generate?inspectionId=invalid-id" -Method Post -ErrorAction SilentlyContinue
# Should return error response
```

**Verification:**
- [ ] API returns appropriate error codes
- [ ] Error messages are helpful
- [ ] Stack traces not exposed to client
- [ ] Application continues running after error

---

## ✅ Security Verification

### CORS Headers
```powershell
$headers = (Invoke-WebRequest -Uri "http://localhost:8080/api/equipment" -Method Options).Headers
Write-Host $headers["Access-Control-Allow-Origin"]
# Should show * or specific domain
```

**Verification:**
- [ ] CORS headers present
- [ ] No security warnings in browser console

### Input Validation
```powershell
# Test with SQL injection attempt
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/search?equipmentId='; DROP TABLE EQUIPMENT;--" -ErrorAction SilentlyContinue
# Should not execute injection
```

**Verification:**
- [ ] Injection attempt doesn't harm system
- [ ] Session remains stable

---

## ✅ Final Sign-Off Checklist

### Core Functionality
- [ ] Database schema working with sample data
- [ ] All 4 entities (Equipment, Inspector, Inspection, Report) functional
- [ ] Relationships/associations working correctly

### API Functionality  
- [ ] All GET endpoints working
- [ ] All POST endpoints working
- [ ] All search filters working
- [ ] Report generation working
- [ ] PDF export working

### Data Integrity
- [ ] No data loss on create/update
- [ ] Referential integrity maintained
- [ ] Sample data intact and accessible

### User Experience
- [ ] Clear error messages
- [ ] Appropriate HTTP status codes
- [ ] Response times acceptable
- [ ] API responses well-formatted

### Documentation
- [ ] All guides complete
- [ ] Examples accurate and working
- [ ] Troubleshooting guides helpful
- [ ] API documentation comprehensive

---

## 🔴 If You Find Issues

### Application Won't Start
1. [ ] Check Java version: `java -version`
2. [ ] Check Maven version: `mvn -version`
3. [ ] Check Node version: `node -v`
4. [ ] Run: `mvn clean install -U`
5. [ ] Check for port conflicts: Try changing port in application.yaml
6. [ ] Review full console output for error messages

### APIs Return Errors
1. [ ] Verify application is running (console shows "Started Application")
2. [ ] Check database is initialized (H2 console accessible)
3. [ ] Verify sample data loaded (H2 console shows records)
4. [ ] Check request URLs for typos
5. [ ] Check PowerShell response for details

### PDF Generation Fails
1. [ ] Verify iText dependency installed: `mvn dependency:tree | grep itext`
2. [ ] Check inspection ID exists in database
3. [ ] Review application console for error details
4. [ ] Verify disk space available
5. [ ] Check file permissions on working directory

### Database Issues
1. [ ] Access H2 Console: http://localhost:8080/h2-console
2. [ ] Verify credentials: username=sa, password=(blank)
3. [ ] Check table structure: `SHOW TABLES;`
4. [ ] Count records: `SELECT COUNT(*) FROM INSPECTION;`
5. [ ] Check for errors: Query execution details

---

## ✅ Success Confirmation

**All checkboxes completed?** 🎉

You have successfully:
- ✅ Set up the project
- ✅ Installed all dependencies
- ✅ Started the application
- ✅ Verified database functionality
- ✅ Tested all API endpoints
- ✅ Generated and exported reports
- ✅ Verified data integrity
- ✅ Confirmed system performance
- ✅ Reviewed documentation

**Status:** ✅ READY FOR USE

---

**Verification Date:** _______________  
**Verified By:** _______________  
**Notes:** _______________________________________________

---

Last Updated: March 13, 2026

