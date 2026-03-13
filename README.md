# Equipment Inspection Report - SAP CAP Project

## Project Overview

This is a **SAP CAP (Core Application Programming) project** built with **Java backend** and **Fiori Elements frontend** for managing equipment inspection records.

**Key Features:**
- Search inspection data by equipment, inspector, date range, and status
- Generate professional PDF inspection reports
- Add manual comments and approval information
- Export reports as PDF for printing
- Fiori List Report UI for data discovery

---

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Backend** | SAP CAP Java | 4.8.0 |
| **Framework** | Spring Boot | 3.5.11 |
| **Database** | H2 (Development) | Latest |
| **Java** | JDK | 21 |
| **Build Tool** | Maven | 3.9+ |
| **PDF Generation** | iText | 7.2.5 |
| **OData** | OData V4 | SAP CAP |

---

## Project Structure

```
equipment-inspection-cap/
├── db/
│   ├── schema.cds                          # CDS database models
│   └── data/
│       ├── equipment.csv                   # Sample equipment data
│       ├── inspector.csv                   # Sample inspector data
│       └── inspection.csv                  # Sample inspection records
│
├── srv/
│   ├── InspectionService.cds               # Service definition
│   ├── InspectionService-annotations.cds   # Fiori annotations
│   ├── pom.xml                             # Maven dependencies
│   └── src/main/java/
│       └── customer/final_course_sap/
│           ├── Application.java            # Spring Boot entry point
│           ├── handlers/
│           │   └── InspectionServiceHandler.java  # Business logic
│           ├── controller/
│           │   └── InspectionController.java     # REST endpoints
│           └── report/
│               └── PdfReportGenerator.java       # PDF generation logic
│   └── src/main/resources/
│       └── application.yaml                # Application configuration
│
├── pom.xml                                 # Parent Maven POM
└── package.json                            # Node.js/npm config
```

---

## Database Schema

### Equipment Table
| Column | Type | Description |
|--------|------|-------------|
| ID | UUID | Primary Key |
| name | String | Equipment name |
| type | String | Equipment type (Hydraulic Press, CNC, etc.) |
| location | String | Physical location |
| serialNumber | String | Manufacturer serial number |
| manufacturer | String | Equipment manufacturer |
| installationDate | Date | Installation date |
| status | String | Active / Inactive / Maintenance |

### Inspector Table
| Column | Type | Description |
|--------|------|-------------|
| ID | UUID | Primary Key |
| name | String | Inspector name |
| employeeId | String | Employee ID |
| department | String | Department |
| email | String | Contact email |
| certifications | String | Professional certifications |

### Inspection Table
| Column | Type | Description |
|--------|------|-------------|
| ID | UUID | Primary Key |
| equipment_ID | UUID | FK to Equipment |
| inspector_ID | UUID | FK to Inspector |
| inspectionDate | Date | Inspection date |
| completionDate | Date | Completion date |
| status | String | Planned / InProgress / Completed / Failed |
| notes | String | Inspection notes |
| findings | String | Detailed findings |
| safetyIssues | String | Safety issues documented |
| nextInspectionDate | Date | Scheduled next inspection |

### InspectionReport Table
| Column | Type | Description |
|--------|------|-------------|
| ID | UUID | Primary Key |
| inspection_ID | UUID | FK to Inspection |
| additionalComments | String | User-added comments |
| approvedBy | String | Approval signature |
| approvalDate | Date | Approval date |
| reportPdf | LargeString | Base64 encoded PDF |

---

## Prerequisites

### System Requirements
- **Windows/Mac/Linux** (any OS supporting Java 21)
- **Java 21 JDK** ([Download](https://www.oracle.com/java/technologies/downloads/#java21))
- **Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))
- **Node.js 18+** ([Download](https://nodejs.org/))
- **Git** (optional, for version control)

### Verify Installation
```bash
# Check Java version
java -version
# Output should show Java 21

# Check Maven version
mvn -version
# Output should show Maven 3.9+

# Check Node.js version
node -v
npm -v
```

---

## Installation & Setup

### Step 1: Clone/Download the Project
```bash
cd d:\Github\final_course_sap
```

### Step 2: Install Node Dependencies
```bash
npm install
```

This installs the CDS toolkit and required Node.js dependencies.

### Step 3: Install Maven Dependencies
```bash
mvn clean install
```

This compiles the CDS models and generates Java POJOs for database entities.

### Step 4: Build the Project
```bash
# From root directory
mvn clean package
```

Or from the srv directory:
```bash
cd srv
mvn clean package
```

---

## Running the Application

### Option 1: Run with Maven (Recommended for Development)
```bash
# From srv directory
cd srv
mvn spring-boot:run
```

**Expected Output:**
```
Started Application in 8.234 seconds (JVM running for 9.156)
```

The application will be available at: **http://localhost:8080**

### Option 2: Run JAR File
After packaging, run the generated JAR:
```bash
java -jar srv/target/final_course_sap-exec.jar
```

### Option 3: Run in IDE
1. Open the project in **IntelliJ IDEA** or **VS Code**
2. Right-click `Application.java` → **Run**
3. Application starts on **http://localhost:8080**

---

## API Endpoints

### Search & List Endpoints

#### 1. **Get All Inspections**
```http
GET http://localhost:8080/api/inspections
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "ID": "5f5f1111-6666-7777-8888-999999999fff",
    "equipment_ID": "8f8f1234-5678-9abc-def0-123456789abc",
    "inspector_ID": "1a1a1111-2222-3333-4444-555555555aaa",
    "inspectionDate": "2025-11-15",
    "completionDate": "2025-11-15",
    "status": "Completed",
    "findings": "All components within normal parameters",
    "safetyIssues": null,
    "notes": "Routine quarterly inspection"
  }
]
```

---

#### 2. **Get Inspection by ID**
```http
GET http://localhost:8080/api/inspections/{id}
```

Example:
```http
GET http://localhost:8080/api/inspections/5f5f1111-6666-7777-8888-999999999fff
```

---

#### 3. **Get All Equipment**
```http
GET http://localhost:8080/api/equipment
```

---

#### 4. **Get All Inspectors**
```http
GET http://localhost:8080/api/inspectors
```

---

#### 5. **Search Inspections (Advanced Filtering)**
```http
GET http://localhost:8080/api/search?equipmentId=8f8f1234-5678-9abc-def0-123456789abc&status=Completed&inspectorId=1a1a1111-2222-3333-4444-555555555aaa
```

**Query Parameters:**
- `equipmentId` (optional): Filter by equipment UUID
- `inspectorId` (optional): Filter by inspector UUID
- `status` (optional): Filter by status (Planned, InProgress, Completed, Failed)

---

### Report Generation Endpoints

#### 6. **Generate Inspection Report**
```http
POST http://localhost:8080/api/reports/generate?inspectionId=5f5f1111-6666-7777-8888-999999999fff&additionalComments=Approved%20for%20production
```

**Query Parameters:**
- `inspectionId` (required): UUID of the inspection
- `additionalComments` (optional): Additional comments to include in report

**Response:**
```json
{
  "inspectionId": "5f5f1111-6666-7777-8888-999999999fff",
  "message": "Report generation initiated",
  "equipment": "Hydraulic Press A1",
  "inspector": "John Smith",
  "status": "Completed"
}
```

---

#### 7. **Export Report as PDF**
```http
GET http://localhost:8080/api/reports/export/{reportId}
```

**Response:** Binary PDF file (downloads inspection_report_{reportId}.pdf)

---

## Using the Application

### Scenario 1: Search Inspections
1. **GET** http://localhost:8080/api/equipment
   - Retrieve equipment list
   
2. **GET** http://localhost:8080/api/search?status=Completed
   - Search completed inspections
   
3. In UI, select an inspection record → Click "View Details"

---

### Scenario 2: Generate and Export Report
1. **GET** http://localhost:8080/api/inspections/{inspectionId}
   - Retrieve inspection details
   
2. **POST** http://localhost:8080/api/reports/generate?inspectionId={id}&additionalComments=QA%20Approved
   - Generate PDF report with additional comments
   
3. **GET** http://localhost:8080/api/reports/export/{reportId}
   - Download generated PDF

---

## Sample Data

### Pre-loaded Equipment
- **Hydraulic Press A1** (Location: Building A - Floor 1)
- **CNC Machine B2** (Location: Building B - Floor 2)
- **Welding Robot C3** (Location: Building A - Floor 3)
- **Compressor Unit D4** (Location: Building C - Basement)
- **Power Generator E5** (Location: Building D - Exterior)

### Pre-loaded Inspectors
- John Smith (Manufacturing Department)
- Sarah Johnson (Quality Assurance)
- Michael Brown (Maintenance)
- Emily Davis (Safety)
- Robert Wilson (Manufacturing)

### Pre-loaded Inspections
- 5 inspection records with various statuses (Completed, InProgress)

**Access Data:**
```bash
# Open H2 Console
http://localhost:8080/h2-console

# Connection details:
# JDBC URL: jdbc:h2:mem:testdb
# Username: sa
# Password: (leave blank)
```

---

## Build & Deploy

### Building for Production

#### Step 1: Clean and Build
```bash
cd srv
mvn clean package -Pproduction
```

#### Step 2: Verify JAR Created
```bash
ls -la target/final_course_sap-exec.jar
```

#### Step 3: Run Production JAR
```bash
java -jar target/final_course_sap-exec.jar
```

---

### Deploy to Cloud (Example)

#### Deploy to Azure App Service
```bash
az webapp up --name equipment-inspection-cap --resource-group myResourceGroup
```

#### Deploy to SAP Cloud Foundry
```bash
cf login
cf push equipment-inspection-cap -p srv/target/final_course_sap-exec.jar
```

---

## Troubleshooting

### Issue 1: Maven Build Fails
**Error:** `ERROR] Failed to execute goal com.sap.cds:cds-maven-plugin`

**Solution:**
```bash
# Clear Maven cache
mvn clean

# Reinstall dependencies
mvn install -U

# Check Node version
node -v  # Should be 18+
```

---

### Issue 2: Port 8080 Already in Use
**Error:** `Address already in use`

**Solution:**
```bash
# Kill process on port 8080 (Windows PowerShell)
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process

# Or change port in application.yaml
# server.port: 8081
```

---

### Issue 3: H2 Database Connection Failed
**Error:** `Cannot get a connection`

**Solution:**
1. Check MySQL/PostgreSQL service is running
2. Verify datasource URL in `application.yaml`
3. Ensure database credentials are correct

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
```

---

### Issue 4: CORS Errors in Frontend
**Error:** `Access to XMLHttpRequest blocked by CORS policy`

**Solution:** Controller already has `@CrossOrigin(origins = "*")` enabled. If still having issues:

In [InspectionController.java](InspectionController.java):
```java
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
```

---

## Performance Tips

1. **Enable Query Caching**
   ```yaml
   cds:
     cqn.cache.enabled: true
   ```

2. **Set Database Connection Pool**
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20
         minimum-idle: 5
   ```

3. **Enable Compression**
   ```yaml
   server:
     compression:
       enabled: true
       min-response-size: 1024
   ```

---

## Testing

### Unit Test Example
Create `InspectionServiceHandlerTest.java`:

```java
@SpringBootTest
class InspectionServiceHandlerTest {
    
    @Autowired
    private PersistenceService persistenceService;
    
    @Test
    void testSearchInspections() {
        // Test implementation
    }
}
```

**Run Tests:**
```bash
mvn test
```

---

## Development Tools

### VS Code Extensions
- **REST Client** - Test APIs directly
- **CDS Extension Pack** - CDS language support
- **Spring Boot Extension Pack** - Java/Spring Boot support

### Postman Collection
Import and use provided Postman collection:
```
File → Import → Select postman_collection.json
```

---

## Key Code Files

| File | Purpose |
|------|---------|
| `db/schema.cds` | Database entity models |
| `srv/InspectionService.cds` | OData service definition |
| `srv/InspectionServiceHandler.java` | Business logic |
| `PdfReportGenerator.java` | PDF generation using iText |
| `InspectionController.java` | REST API endpoints |
| `application.yaml` | Configuration |

---

## Maintenance & Updates

### Add New Equipment Type
1. Edit `db/schema.cds` - Add enum value
2. Update `db/data/equipment.csv` with new type
3. Rebuild: `mvn clean build`

### Modify Report Template
1. Edit `PdfReportGenerator.java`
2. Add new sections or fields
3. Rebuild and redeploy

### Update Inspection Status
1. Edit `db/schema.cds` - Update status enum
2. Update UI annotations
3. Rebuild and test

---

## Additional Resources

- [SAP CAP Documentation](https://cap.cloud.sap/docs/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [iText PDF Documentation](https://itextpdf.com/en/products/itext-7/itext-7-core)
- [OData V4 Standard](https://www.odata.org/)
- [H2 Database Documentation](http://www.h2database.com/)

---

## Support & Contribution

For issues or questions:
1. Check the troubleshooting section
2. Review application logs in `logs/` folder
3. Enable DEBUG logging in `application.yaml`
4. Contact SAP support or raise a GitHub issue

---

## License

This project is provided as-is for educational and business purposes.

---

**Last Updated:** March 2026
**Version:** 1.0.0
**Maintained By:** SAP Development Team

