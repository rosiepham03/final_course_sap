# Development Guide - Equipment Inspection Report

This guide helps developers extend and customize the application.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                  Frontend (Fiori UI)                 │
│            (Fiori Elements + CDS Annotations)        │
└──────────────────────┬──────────────────────────────┘
                       │ OData V4 Calls (CDS Native)
                       │
┌──────────────────────▼──────────────────────────────┐
│       OData Service Layer (CDS Native)              │
│    (InspectionService.cds) [SAP CAP Built-in]      │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│       Service Handler Layer                         │
│     (InspectionServiceHandler.java)                 │
│      - Business Logic                               │
│      - Search & Filtering                           │
│      - Report Generation                            │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│         CDS Data Layer                              │
│      - Persistence Service                          │
│      - CQL Queries                                  │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│         Database (H2 / PostgreSQL)                  │
│      Equipment, Inspector, Inspection, Report       │
└─────────────────────────────────────────────────────┘
```

---

## 📁 File Structure & Responsibilities

### Core Data Models
**File:** `db/schema.cds`

Contains:
- Equipment entity
- Inspector entity
- Inspection entity
- InspectionReport entity

**To add new entity:**
```cds
// db/schema.cds
namespace equipment.inspection;

entity MaintenanceSchedule : cuid {
    equipment: Association to Equipment;
    scheduledDate: Date;
    maintenanceType: String;
    estimatedHours: Decimal;
}
```

---

### Service Definition
**File:** `srv/InspectionService.cds`

Defines:
- OData entities
- Function imports
- Custom types

**To add new function:**
```cds
function scheduleMaintenence(
    equipmentId: String,
    scheduledDate: Date,
    maintenanceType: String
) returns { success: Boolean; scheduleId: String };
```

---

### Fiori Annotations
**File:** `srv/InspectionService-annotations.cds`

Controls UI rendering:
- List Report columns
- Object Page facets
- Field behavior

**To customize List Report:**
```cds
annotate InspectionService.Inspection with @(
    UI.LineItem: [
        { Value: equipment.name, Label: 'Equipment' },
        { Value: inspector.name, Label: 'Inspector' },
        { Value: inspectionDate, Label: 'Date' },
        { Value: status, Label: 'Status', Criticality: status }
    ]
);
```

---

### Java Service Handler
**File:** `srv/src/main/java/customer/final_course_sap/handlers/InspectionServiceHandler.java`

Contains:
- `@On` event handlers
- Business logic
- Data transformation

**To handle new function:**
```java
@On(event = "scheduleMaintenence")
public Map<String, Object> handleScheduleMaintenance(ScheduleMaintenenceContext context) {
    // Implementation
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("success", true);
    response.put("scheduleId", "new-uuid");
    return response;
}
```

---

### PDF Generator
**File:** `srv/src/main/java/customer/final_course_sap/report/PdfReportGenerator.java`

Uses **iText 7** for PDF generation.

**To customize report template:**
```java
// Add new section to PDF
Paragraph maintenanceHeader = new Paragraph("MAINTENANCE HISTORY")
    .setFont(boldFont)
    .setFontSize(14);
document.add(maintenanceHeader);

// Add maintenance records table
Table maintenanceTable = new Table(3);
maintenanceTable.addCell(createCell("Date", boldFont));
maintenanceTable.addCell(createCell("Type", boldFont));
maintenanceTable.addCell(createCell("Hours", boldFont));

document.add(maintenanceTable);
```

---

## 🔧 Common Customization Tasks

### Task 1: Add New Status to Inspection

**Step 1:** Update enum in `db/schema.cds`
```cds
status: String enum { 
    Planned; 
    InProgress; 
    Completed; 
    Failed;
    OnHold          // ← New status
}
```

**Step 2:** Rebuild
```bash
mvn clean build
```

**Step 3:** Update UI annotations if needed
```cds
// srv/InspectionService-annotations.cds
$Type: 'UI.CriticalityType',
Value: status,
Criticality: {
    $edmType: 'Edm.String',
    0: status = 'OnHold'  // ← Yellow indicator
}
```

---

### Task 2: Add New Search Filter

**Step 1:** Add parameter to function in `srv/InspectionService.cds`
```cds
function searchInspections(
    equipmentId: String,
    equipmentName: String,
    inspectionDateFrom: Date,
    inspectionDateTo: Date,
    inspectorId: String,
    status: String,
    location: String    // ← New filter
) returns array of InspectionSearchResult;
```

**Step 2:** Implement handler in `InspectionServiceHandler.java`
```java
private boolean filterByLocation(InspectionItems inspection, String location) {
    if (location == null || location.isEmpty()) {
        return true;
    }
    // Fetch equipment and compare location
    Optional<EquipmentItems> equipment = getEquipmentDetails(inspection.getEquipmentID());
    return equipment.map(e -> location.equals(e.getLocation())).orElse(false);
}
```

**Step 3:** Apply filter in search method
```java
.filter(i -> filterByLocation(i, context.getLocation()))
```

---

### Task 3: Add Email Notifications

**Step 1:** Add dependency to `pom.xml`
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

**Step 2:** Create mail service
```java
@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendReportNotification(String inspectorEmail, String reportId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(inspectorEmail);
        message.setSubject("Equipment Inspection Report Ready");
        message.setText("Your inspection report is ready. ID: " + reportId);
        mailSender.send(message);
    }
}
```

**Step 3:** Call from handler
```java
@On(event = "generateReport")
public ReportGenerationResponse handleGenerateReport(GenerateReportContext context) {
    // ... existing code ...
    
    // Send notification
    emailService.sendReportNotification(
        inspector.get().getEmail(),
        report.getId()
    );
    
    return response;
}
```

---

### Task 4: Add Reporting/Analytics

**Step 1:** Create new CDS entity
```cds
// db/schema.cds
entity InspectionMetrics : cuid {
    month: String;
    totalInspections: Integer;
    completedInspections: Integer;
    failedInspections: Integer;
    averageCompletionDays: Decimal;
}
```

**Step 2:** Add analytics function
```cds
// srv/InspectionService.cds
function getMonthlyMetrics(year: Integer, month: Integer) 
    returns array of InspectionMetrics;
```

**Step 3:** Implement calculation logic
```java
@On(event = "getMonthlyMetrics")
public List<Map<String, Object>> handleGetMonthlyMetrics(GetMonthlyMetricsContext context) {
    // Query and aggregate inspection data
    // Calculate metrics
    // Return results
}
```

---

## 🗄️ Database Operations

### Create New Record
```java
InspectionItems newInspection = new InspectionItems();
newInspection.setEquipmentID("equipment-uuid");
newInspection.setInspectorID("inspector-uuid");
newInspection.setInspectionDate(LocalDate.now());
newInspection.setStatus("Planned");

persistenceService.run(
    Insert.into(Inspection_.class).entry(newInspection)
);
```

---

### Update Record
```java
persistenceService.run(
    Update.entity(Inspection_.class)
        .data(inspection)
        .where(i -> i.ID().eq(inspectionId))
);
```

---

### Delete Record
```java
persistenceService.run(
    Delete.from(Inspection_.class)
        .where(i -> i.ID().eq(inspectionId))
);
```

---

### Complex Query with Joins
```java
List<InspectionItems> results = persistenceService.run(
    Select.from(Inspection_.class)
        .where(i -> i.status().eq("Completed")
            .and(i.inspectionDate().ge(startDate))
            .and(i.inspectionDate().le(endDate)))
        .orderBy(i -> i.inspectionDate().desc())
).listOf(InspectionItems.class);
```

---

## 🧪 Testing

### Unit Test Template
```java
@SpringBootTest
@ActiveProfiles("test")
class InspectionServiceHandlerTest {
    
    @Autowired
    private InspectionServiceHandler handler;
    
    @Autowired
    private PersistenceService persistenceService;
    
    @BeforeEach
    void setUp() {
        // Test data setup
    }
    
    @Test
    void testSearchInspections_ReturnsCompleted() {
        // Arrange
        SearchInspectionsContext context = new SearchInspectionsContext();
        context.setStatus("Completed");
        
        // Act
        List<InspectionSearchResult> results = handler.handleSearchInspections(context);
        
        // Assert
        assertNotNull(results);
        assertTrue(results.stream().allMatch(r -> "Completed".equals(r.getStatus())));
    }
    
    @Test
    void testGenerateReport_Success() {
        // Test implementation
    }
}
```

---

### Integration Test Example
```java
@SpringBootTest
@AutoConfigureMockMvc
class InspectionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetAllInspections() throws Exception {
        mockMvc.perform(get("/api/inspections"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }
}
```

---

## 📝 Logging & Debugging

### Add Debug Logging
```java
logger.debug("Processing inspection: {}", inspectionId);
logger.info("Report generated successfully for: {}", equipmentName);
logger.warn("No inspections found for equipment: {}", equipmentId);
logger.error("Failed to generate PDF report", exception);
```

### Configure Logging Levels
```yaml
# application.yaml
logging:
  level:
    root: INFO
    customer.final_course_sap: DEBUG
    com.sap.cds: INFO
    org.springframework.web: DEBUG
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 30
```

---

## 🔒 Security Enhancements

### Add Spring Security
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/**").authenticated()
            .anyRequest().authenticated()
        );
        return http.build();
    }
}
```

---

### Add Request Validation
```java
@PostMapping("/reports/generate")
public ResponseEntity<?> generateReport(
    @Valid @RequestParam String inspectionId,
    @Length(min = 0, max = 500) String additionalComments) {
    // Implementation
}
```

---

## 📚 Deployment Checklist

- [ ] Update version number in `pom.xml`
- [ ] Run full test suite: `mvn test`
- [ ] Check code quality: `mvn sonar:sonar`
- [ ] Build JAR: `mvn clean package`
- [ ] Test JAR locally: `java -jar srv/target/*.jar`
- [ ] Update API documentation
- [ ] Tag version in Git: `git tag v1.x.x`
- [ ] Deploy to staging environment
- [ ] Run smoke tests
- [ ] Deploy to production
- [ ] Verify in production
- [ ] Update release notes

---

## 🚀 Performance Optimization

### 1. Database Query Optimization
```java
// Before: Multiple queries N+1 problem
List<InspectionItems> inspections = persistenceService.run(
    Select.from(Inspection_.class)
).listOf(InspectionItems.class);

for (InspectionItems insp : inspections) {
    // This causes N additional queries
    EquipmentItems equipment = getEquipment(insp.getEquipmentID());
}

// After: Single query with better filtering
List<InspectionItems> inspections = persistenceService.run(
    Select.from(Inspection_.class)
        .where(i -> i.status().eq("Completed"))
).listOf(InspectionItems.class);
```

---

### 2. Response Caching
```java
@GetMapping("/equipment")
@Cacheable(value = "equipment", ttl = 3600)
public ResponseEntity<?> getAllEquipment() {
    // Results cached for 1 hour
}
```

---

### 3. Lazy Loading
```cds
// db/schema.cds
entity Inspection : cuid {
    equipment: Association to Equipment on Association.target == $self.equipment;
    inspector: Association to Inspector on Association.target == $self.inspector;
    inspectionReport: Composition of many InspectionReport on 
        inspectionReport.inspection == $self;
}
```

---

## 🐛 Debugging Tips

### Enable Debug Mode
```bash
# Run with debug enabled
mvn spring-boot:run -Ddebug=true
```

### View Generated SQL
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.dialect.Dialect: DEBUG
```

### Check H2 Console
Access `http://localhost:8080/h2-console` and run queries directly:
```sql
SELECT * FROM INSPECTION WHERE STATUS = 'Failed';
SELECT EQUIPMENT_ID, COUNT(*) FROM INSPECTION GROUP BY EQUIPMENT_ID;
```

---

## 📞 Troubleshooting Development Issues

| Issue | Solution |
|-------|----------|
| Class not found after adding new entity | Run `mvn clean cds:generate` |
| Changes not reflected after restart | Delete `/target` folder and rebuild |
| H2 console not accessible | Check `application.yaml` has `h2.console.enabled: true` |
| Port in use | Change `server.port` in `application.yaml` |
| PDF generation fails | Check iText dependency: `mvn dependency:tree \| grep itext` |

---

Last Updated: March 13, 2026
Version: 1.0.0

