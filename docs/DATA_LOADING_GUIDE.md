# 📊 Data Loading to Backend

## Overview
Dữ liệu từ các tệp CSV trong `db/data/` sẽ **tự động được tải vào database** khi ứng dụng khởi động.

## Architecture

### Components
- **DataInitializer.java**: Spring Boot component tự động tải dữ liệu
- **CSV Files**: Nguồn dữ liệu ban đầu
  - `db/data/equipment.csv` - Dữ liệu thiết bị
  - `db/data/inspector.csv` - Dữ liệu kiểm tra viên
  - `db/data/inspection.csv` - Dữ liệu phiếu kiểm tra

### Database
- **Type**: H2 (In-memory database)
- **Connection**: `jdbc:h2:mem:testdb`
- **Access**: http://localhost:8080/h2-console

## How It Works

### 1. Application Startup
```
Application Starts
    ↓
Spring Boot initializes
    ↓
DataInitializer bean is created
    ↓
ApplicationRunner registered
    ↓
Data loading begins
```

### 2. Data Loading Sequence
```
1. Equipment data loaded
   └─ Creates Equipment records in database
   
2. Inspector data loaded
   └─ Creates Inspector records in database
   
3. Inspection data loaded
   └─ Creates Inspection records with references
```

### 3. Logging Output
Kiểm tra console logs để xác nhận dữ liệu được tải:
```
Starting data initialization...
Loading equipment data...
Data directory found at: D:\Github\final_course_sap\db\data
Loaded 5 equipment records
Loading inspector data...
Loaded 5 inspector records
Loading inspection data...
Loaded 5 inspection records
Data initialization completed successfully
```

## Data Structure

### Equipment CSV
```
ID,name,type,location,serialNumber,manufacturer,installationDate,status
8f8f1234-5678-9abc-def0-123456789abc,Hydraulic Press A1,Hydraulic Press,...
```

**Fields:**
- `ID`: Unique identifier (UUID)
- `name`: Equipment name
- `type`: Equipment type (Hydraulic Press, CNC Machine, etc.)
- `location`: Physical location
- `serialNumber`: Serial number
- `manufacturer`: Manufacturer name
- `installationDate`: Installation date (YYYY-MM-DD)
- `status`: Status (Active, Inactive, Maintenance)

### Inspector CSV
```
ID,name,employeeId,department,email,certifications
1a1a1111-2222-3333-4444-555555555aaa,John Smith,EMP001,...
```

**Fields:**
- `ID`: Unique identifier (UUID)
- `name`: Inspector name
- `employeeId`: Employee ID
- `department`: Department
- `email`: Email address
- `certifications`: Certifications (comma-separated)

### Inspection CSV
```
ID,equipment_ID,inspector_ID,inspectionDate,completionDate,status,notes,findings,safetyIssues,nextInspectionDate
5f5f1111-6666-7777-8888-999999999fff,8f8f1234-5678-9abc-def0-123456789abc,...
```

**Fields:**
- `ID`: Unique identifier (UUID)
- `equipment_ID`: Reference to Equipment
- `inspector_ID`: Reference to Inspector
- `inspectionDate`: Inspection date
- `completionDate`: Completion date
- `status`: Status (Planned, InProgress, Completed, Failed)
- `notes`: Notes
- `findings`: Findings
- `safetyIssues`: Safety issues found
- `nextInspectionDate`: Next inspection date

## Adding New Data

### Option 1: Add to CSV Files
1. Close the application
2. Edit the CSV files in `db/data/`
3. Add new rows following the same format
4. Save the files
5. Restart the application

**Example - Adding equipment:**
```csv
8f8f6789-9abc-def0-1234-567890123xyz,New Equipment,Type,Location,SN-2023-0006,Manufacturer,2023-01-15,Active
```

### Option 2: Use API to Create Data at Runtime
```bash
POST http://localhost:8080/odata/v4/InspectionService/Equipment
{
  "ID": "unique-uuid",
  "name": "Equipment Name",
  "type": "Type",
  "location": "Location",
  "serialNumber": "SN-2023-0007",
  "manufacturer": "Manufacturer",
  "installationDate": "2023-01-15",
  "status": "Active"
}
```

## Troubleshooting

### Issue: Data not loaded
**Solution:**
- Check console logs for errors
- Verify CSV files exist in `db/data/`
- Verify CSV format is correct (check headers match exactly)
- Verify file encoding is UTF-8

### Issue: Date parsing errors
**Solution:**
- Ensure dates are in `YYYY-MM-DD` format
- Check for extra spaces in date fields

### Issue: CSV file not found
**Solution:**
- DataInitializer tries multiple paths:
  - `db/data`
  - `../db/data`
  - `../../db/data`
  - `{user.dir}/db/data`
- Ensure you're running from the correct directory

## Updating Data

### Modify Existing Records
Use the OData API:
```bash
PATCH http://localhost:8080/odata/v4/InspectionService/Equipment/{ID}
{
  "status": "Maintenance"
}
```

### Delete Records
```bash
DELETE http://localhost:8080/odata/v4/InspectionService/Equipment/{ID}
```

## Performance Notes

- **Initial Load**: ~100ms for 5 records each
- **In-Memory Database**: Data persists during session, cleared on restart
- **Scalability**: Suitable for development/testing with <1000 records

## Production Considerations

For production deployment:
1. Use persistent database (PostgreSQL, SQL Server, etc.)
2. Implement data migration scripts
3. Use database initialization tools (Flyway, Liquibase)
4. Externalize data loading logic
5. Add retry mechanisms and error handling

## Files Modified/Created

- ✅ [DataInitializer.java](srv/src/main/java/customer/final_course_sap/handlers/DataInitializer.java) - Data loading component
- ✅ [pom.xml](srv/pom.xml) - Added OpenCSV dependency
- 📂 [db/data/](db/data/) - CSV data files

## Related Documentation

- [Equipment Inspection API Documentation](API_DOCUMENTATION.md)
- [Database Schema](db/schema.cds)
- [Service Definition](srv/InspectionService.cds)
