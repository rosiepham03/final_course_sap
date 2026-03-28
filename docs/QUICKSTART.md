# Quick Start Guide - Equipment Inspection Report

## ⚡ Get Running in 5 Minutes

### Prerequisites Check
```bash
java -version          # Must be Java 21+
mvn -version          # Must be Maven 3.9+
node -v               # Must be Node 18+
```

---

## 🚀 Quick Start Steps

### Step 1: Navigate to Project
```bash
cd d:\Github\final_course_sap
```

### Step 2: Install Dependencies (First Time Only)
```bash
npm install
mvn clean install
```

### Step 3: Build Project
```bash
mvn clean package
```

### Step 4: Start Application
```bash
cd srv
mvn spring-boot:run
```

### Step 5: Verify Running
Open browser: **http://localhost:8080**

Should see Spring Boot started message in console.

---

## 📋 Test the APIs

### Using PowerShell (Windows)

#### 1. Get All Inspections
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/inspections" -Method Get
$response | ConvertTo-Json | Write-Host
```

#### 2. Get All Equipment
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/equipment" -Method Get
$response | ConvertTo-Json | Write-Host
```

#### 3. Search Inspections (Completed only)
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/search?status=Completed" -Method Get
$response | ConvertTo-Json | Write-Host
```

#### 4. Generate Report
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/reports/generate?inspectionId=5f5f1111-6666-7777-8888-999999999fff&additionalComments=Approved" -Method Post
$response | ConvertTo-Json | Write-Host
```

---

### Using cURL (Alternative)

#### Get Inspections
```bash
curl http://localhost:8080/api/inspections
```

#### Search
```bash
curl "http://localhost:8080/api/search?status=Completed"
```

#### Generate Report
```bash
curl -X POST "http://localhost:8080/api/reports/generate?inspectionId=5f5f1111-6666-7777-8888-999999999fff"
```

---

## 📊 Database Console

### Access H2 Console
1. Open: **http://localhost:8080/h2-console**
2. Login:
   - **JDBC URL:** `jdbc:h2:mem:testdb`
   - **Username:** `sa`
   - **Password:** (leave empty)
   - Click **Connect**

### View Sample Data
```sql
-- View all equipment
SELECT * FROM EQUIPMENT;

-- View all inspectors
SELECT * FROM INSPECTOR;

-- View all inspections
SELECT * FROM INSPECTION;

-- View completed inspections
SELECT * FROM INSPECTION WHERE STATUS = 'Completed';

-- Count inspections by status
SELECT STATUS, COUNT(*) as COUNT FROM INSPECTION GROUP BY STATUS;
```

---

## 🧪 Using REST Client in VS Code

### Install Extension
1. Open VS Code
2. Extensions → Search "REST Client"
3. Install by Huachao Mao

### Create Test File (test.http)
```http
### Get all inspections
GET http://localhost:8080/api/inspections

### Get equipment list
GET http://localhost:8080/api/equipment

### Search completed inspections
GET http://localhost:8080/api/search?status=Completed

### Generate report
POST http://localhost:8080/api/reports/generate?inspectionId=5f5f1111-6666-7777-8888-999999999fff&additionalComments=Approved

### Export PDF
GET http://localhost:8080/api/reports/export/REPORT_ID_HERE
```

Click **Send Request** above each request to execute.

---

## 📱 Project Structure Review

```
final_course_sap/
├── db/
│   ├── schema.cds              ← Database models
│   └── data/                   ← Sample CSV data
│
├── srv/
│   ├── InspectionService.cds   ← OData service
│   ├── src/main/java/
│   │   └── customer/final_course_sap/
│   │       ├── handlers/       ← Business logic

│   │       └── report/         ← PDF generation
│   └── pom.xml                 ← Dependencies
│
└── README.md                   ← Full documentation
```

---

## 🐛 Common Issues & Fixes

| Issue | Fix |
|-------|-----|
| **Maven build fails** | Run `mvn clean install -U` |
| **Port 8080 in use** | Change port in `srv/src/main/resources/application.yaml` |
| **Java version error** | Install Java 21 SDK |
| **Node not found** | Install Node.js from nodejs.org |
| **CORS errors** | Handled by CDS OData layer |

---

## 📝 Key Files to Understand

1. **Database Schema** - `db/schema.cds`
   - Equipment, Inspector, Inspection entities
   - Relationships and constraints

2. **Service Logic** - `srv/InspectionServiceHandler.java`
   - Search implementation
   - Report generation logic

3. **Service APIs** - CDS OData endpoints (auto-generated from service definition)
   - All API endpoints
   - Request/response handling

4. **PDF Generation** - `srv/src/main/java/report/PdfReportGenerator.java`
   - Professional report layout
   - iText implementation

---

## 🎯 Next Steps

1. ✅ Start the application (Step 4 above)
2. ✅ Test APIs using PowerShell/cURL (Step 2 in Testing section)
3. ✅ Access H2 Console to view data (Database Console section)
4. ✅ Generate and export a report
5. 📖 Read full README.md for advanced features

---

## 💡 Tips

- **Auto-reload enabled:** Changes in `.cds` or `.java` will auto-compile
- **Debug mode:** Add breakpoints in `InspectionServiceHandler.java`
- **View logs:** Check Spring Boot console for detailed logging
- **Modify sample data:** Edit CSV files in `db/data/` and rebuild

---

## 🔗 API Reference (Quick)

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/inspections` | List all inspections |
| GET | `/api/equipment` | List all equipment |
| GET | `/api/inspectors` | List all inspectors |
| GET | `/api/search` | Advanced search with filters |
| POST | `/api/reports/generate` | Generate PDF report |
| GET | `/api/reports/export/{id}` | Download PDF report |

---

## ✨ Success Indicators

When everything is working:
- ✅ Application starts on http://localhost:8080
- ✅ API responses return JSON with sample data
- ✅ H2 Console accessible with data visible
- ✅ Report generation completes successfully
- ✅ PDF can be exported and opened

---

**You're ready to go!** 🎉

For detailed documentation, see **README.md**

---

Last Updated: March 13, 2026
