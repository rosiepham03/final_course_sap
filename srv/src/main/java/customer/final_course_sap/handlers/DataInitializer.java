package customer.final_course_sap.handlers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.sap.cds.ql.Insert;
import com.sap.cds.services.persistence.PersistenceService;

/**
 * Data Initializer - Loads initial data from CSV files into the database
 * Runs on application startup
 */
@Component
public class DataInitializer {

  private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
  private final Map<String, String> equipmentIdMap = new HashMap<>();
  private final Map<String, String> inspectorIdMap = new HashMap<>();

  private final PersistenceService persistenceService;

  public DataInitializer(PersistenceService persistenceService) {
    this.persistenceService = persistenceService;
  }

  /**
   * Initialize data on application startup
   */
  @Bean
  public ApplicationRunner initializeData() {
    return args -> {
      try {
        logger.info("Starting data initialization...");

        // Load data in order of dependencies
        loadEquipmentData();
        loadInspectorData();
        loadInspectionData();

        logger.info("Data initialization completed successfully");
      } catch (Exception e) {
        logger.error("Error during data initialization", e);
      }
    };
  }

  /**
   * Get the path to the data directory
   */
  private Path getDataDirectory() {
    // Try multiple possible paths
    String[] possiblePaths = {
        "db/data",
        "../db/data",
        "../../db/data",
        System.getProperty("user.dir") + "/db/data"
    };

    for (String pathStr : possiblePaths) {
      Path path = Paths.get(pathStr);
      if (Files.exists(path) && Files.isDirectory(path)) {
        logger.info("Data directory found at: {}", path.toAbsolutePath());
        return path;
      }
    }

    logger.warn("Data directory not found in any expected location");
    return Paths.get("db/data");
  }

  /**
   * Parse CSV line into fields
   */
  private String[] parseCSVLine(String line) {
    return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
  }

  /**
   * Validate UUID format (hex only: 0-9, a-f)
   */
  private boolean isValidUUID(String uuid) {
    return uuid.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
  }

  /**
   * Load equipment data from CSV
   */
  private void loadEquipmentData() throws Exception {
    logger.info("Loading equipment data...");
    Path dataDir = getDataDirectory();
    Path filePath = dataDir.resolve("equipment.csv");

    if (!Files.exists(filePath)) {
      logger.warn("Equipment CSV file not found at: {}", filePath.toAbsolutePath());
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
      String headerLine = reader.readLine(); // Skip header
      if (headerLine == null) {
        logger.warn("Equipment CSV file is empty");
        return;
      }

      String line;
      int count = 0;

      while ((line = reader.readLine()) != null) {
        if (line.trim().isEmpty())
          continue;

        String[] fields = parseCSVLine(line);

        if (fields.length < 8) {
          logger.warn("Skipping invalid equipment record: {}", line);
          continue;
        }

        try {
          String csvId = fields[0].trim();
          String validUuid = isValidUUID(csvId) ? csvId : UUID.randomUUID().toString();
          equipmentIdMap.put(csvId, validUuid);

          Map<String, Object> data = new HashMap<>();
          data.put("ID", validUuid);
          data.put("name", fields[1].trim());
          data.put("type", fields[2].trim());
          data.put("location", fields[3].trim());
          data.put("serialNumber", fields[4].trim());
          data.put("manufacturer", fields[5].trim());

          try {
            data.put("installationDate", LocalDate.parse(fields[6].trim(), DATE_FORMATTER));
          } catch (Exception e) {
            logger.warn("Invalid date for equipment {}: {}", csvId, fields[6]);
          }

          data.put("status", fields[7].trim());

          persistenceService.run(Insert.into("equipment.inspection.Equipment").entry(data));
          count++;
        } catch (Exception e) {
          logger.warn("Error loading equipment record: {}", line, e);
        }
      }

      logger.info("Loaded {} equipment records", count);
    }
  }

  /**
   * Load inspector data from CSV
   */
  private void loadInspectorData() throws Exception {
    logger.info("Loading inspector data...");
    Path dataDir = getDataDirectory();
    Path filePath = dataDir.resolve("inspector.csv");

    if (!Files.exists(filePath)) {
      logger.warn("Inspector CSV file not found at: {}", filePath.toAbsolutePath());
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
      String headerLine = reader.readLine(); // Skip header
      if (headerLine == null) {
        logger.warn("Inspector CSV file is empty");
        return;
      }

      String line;
      int count = 0;

      while ((line = reader.readLine()) != null) {
        if (line.trim().isEmpty())
          continue;

        String[] fields = parseCSVLine(line);

        if (fields.length < 6) {
          logger.warn("Skipping invalid inspector record: {}", line);
          continue;
        }

        try {
          String csvId = fields[0].trim();
          String validUuid = isValidUUID(csvId) ? csvId : UUID.randomUUID().toString();
          inspectorIdMap.put(csvId, validUuid);

          Map<String, Object> data = new HashMap<>();
          data.put("ID", validUuid);
          data.put("name", fields[1].trim());
          data.put("employeeId", fields[2].trim());
          data.put("department", fields[3].trim());
          data.put("email", fields[4].trim());
          data.put("certifications", fields[5].trim());

          persistenceService.run(Insert.into("equipment.inspection.Inspector").entry(data));
          count++;
        } catch (Exception e) {
          logger.warn("Error loading inspector record: {}", line, e);
        }
      }

      logger.info("Loaded {} inspector records", count);
    }
  }

  /**
   * Load inspection data from CSV
   */
  private void loadInspectionData() throws Exception {
    logger.info("Loading inspection data...");
    Path dataDir = getDataDirectory();
    Path filePath = dataDir.resolve("inspection.csv");

    if (!Files.exists(filePath)) {
      logger.warn("Inspection CSV file not found at: {}", filePath.toAbsolutePath());
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
      String headerLine = reader.readLine(); // Skip header
      if (headerLine == null) {
        logger.warn("Inspection CSV file is empty");
        return;
      }

      String line;
      int count = 0;

      while ((line = reader.readLine()) != null) {
        if (line.trim().isEmpty())
          continue;

        String[] fields = parseCSVLine(line);

        if (fields.length < 10) {
          logger.warn("Skipping invalid inspection record: {}", line);
          continue;
        }

        try {
          String csvId = fields[0].trim();
          String validUuid = isValidUUID(csvId) ? csvId : UUID.randomUUID().toString();

          String equipmentCsvId = fields[1].trim();
          String equipmentId = equipmentIdMap.getOrDefault(equipmentCsvId, UUID.randomUUID().toString());

          String inspectorCsvId = fields[2].trim();
          String inspectorId = inspectorIdMap.getOrDefault(inspectorCsvId, UUID.randomUUID().toString());

          Map<String, Object> data = new HashMap<>();
          data.put("ID", validUuid);
          data.put("equipment_ID", equipmentId);
          data.put("inspector_ID", inspectorId);

          try {
            data.put("inspectionDate", LocalDate.parse(fields[3].trim(), DATE_FORMATTER));
          } catch (Exception e) {
            logger.warn("Invalid inspection date for inspection {}: {}", csvId, fields[3]);
          }

          try {
            data.put("completionDate", LocalDate.parse(fields[4].trim(), DATE_FORMATTER));
          } catch (Exception e) {
            logger.warn("Invalid completion date for inspection {}: {}", csvId, fields[4]);
          }

          data.put("status", fields[5].trim());
          data.put("notes", fields[6].trim());
          data.put("findings", fields[7].trim());
          data.put("safetyIssues", fields[8].trim());

          try {
            data.put("nextInspectionDate", LocalDate.parse(fields[9].trim(), DATE_FORMATTER));
          } catch (Exception e) {
            logger.warn("Invalid next inspection date for inspection {}: {}", csvId, fields[9]);
          }

          persistenceService.run(Insert.into("equipment.inspection.Inspection").entry(data));
          count++;
        } catch (Exception e) {
          logger.warn("Error loading inspection record: {}", line, e);
        }
      }

      logger.info("Loaded {} inspection records", count);
    }
  }
}
