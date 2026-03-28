# Equipment Inspection Report - SAP CAP Project

## Project Overview

**Key Features:**
- Search inspection data by equipment, inspector, date range, and status
- Generate professional PDF inspection reports
- Add manual comments and approval information
- Export reports as PDF for printing

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

## Prerequisites

### System Requirements
- **Windows/Mac/Linux** (any OS supporting Java 21)
- **Java 21 JDK** ([Download](https://www.oracle.com/java/technologies/downloads/#java21))
- **Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))
- **Node.js 18+** ([Download](https://nodejs.org/))

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
git clone https://github.com/rosiepham03/final_course_sap.git
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
**Maintained By:** Rosie Pham

