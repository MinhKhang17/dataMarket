package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.ValidationErrorDto;
import com.example.datasetapi.model.Dataset.*;
import com.example.datasetapi.repository.DatasetValidationErrorRepository;
import com.example.datasetapi.service.dataset.DatasetService;
import com.example.datasetapi.enums.Datasets.*;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.repository.DatasetInforRepository;
import com.example.datasetapi.repository.DatasetTypeRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.beans.Transient;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DatasetValidateServiceImpl implements DatasetValidateService {

    private static final Set<String> CONNECTOR_ALLOWED = Set.of("CCS1", "CCS2", "CHAdeMO", "Type2", "GB/T");
    private static final Set<String> PRICING_MODEL_ALLOWED = Set.of("Flat", "Time-based", "Energy-based", "Subscription");

    private final DatasetValidationErrorRepository errorRepository;
    @Autowired
    private DatasetTypeRepository datasetTypeRepo;
    @Autowired
    private DatasetInforRepository datasetInforRepository;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    public DatasetValidateServiceImpl(DatasetValidationErrorRepository errorRepository) {
        this.errorRepository = errorRepository;
    }

    // ================================================================
    // 1️⃣ Upload file and validate header (schema)
    // ================================================================
    @Override
    public ResponseEntity<?> uploadAndHeaderCheckCSVFile(Long datasetTypeId, MultipartFile file,
                                                         String name, String description, HttpServletRequest request) {
        log.info("[UPLOAD] Starting dataset header validation: name='{}', datasetTypeId={}", name, datasetTypeId);
        try {
            String token = tokenService.resolveToken(request);
            long providerId = jwtUtil.getUserIdFromToken(token);
            Provider provider = userService.findProviderById(providerId);

            DatasetType type = datasetTypeRepo.findById(datasetTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("DatasetType not found: " + datasetTypeId));

            String path = saveTemp(file);
            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }

            Dataset dataset = new Dataset();
            dataset.setDescription(description);
            dataset.setDatasetType(type);

            DatasetInformation ds = new DatasetInformation();
            ds.setName(name);
            ds.setFile_url(path);
            ds.setStatus(DatasetInforStatus.PENDING);
            ds.setDatasetExtension(FileExtension.valueOf(extension.toUpperCase(Locale.ROOT)));
            ds = datasetInforRepository.save(ds);

            log.info("[UPLOAD] Reading CSV file for header validation...");
            try (Reader r = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
                CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r);

                List<ValidationErrorDto> errors = validateSchema(type, parser.getHeaderMap().keySet(), parser.getRecords().size());

                if (!errors.isEmpty()) {
                    ds.setStatus(DatasetInforStatus.SCHEMA_FAILED);
                    ds.setValidationErrors(errors);
                    ds = datasetInforRepository.save(ds);
                    log.warn("[UPLOAD] Schema validation failed with {} errors.", errors.size());
                } else {
                    ds.setStatus(DatasetInforStatus.PENDING_MODERATION);
                    ds.setRowCount((long) parser.getRecords().size());
                    ds = datasetInforRepository.save(ds);
                    // Commented for now: datasetService.uploadCSVFileToPendingFolder(file, dataset);
                    log.info("[UPLOAD] Header validation passed, dataset is ready for moderation.");
                }
            }
            return ResponseEntity.ok(new ApiResponse(true, "Header validation successful", ds));

        } catch (IllegalArgumentException e) {
            log.warn("[UPLOAD] Invalid input parameter: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (IOException e) {
            log.error("[UPLOAD] Error reading/saving CSV file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse(false, "Error while reading or saving CSV file", null));
        } catch (Exception e) {
            log.error("[UPLOAD] Unexpected error while uploading dataset", e);
            return ResponseEntity.internalServerError().body(new ApiResponse(false, "Unexpected system error while uploading dataset", null));
        }
    }

    // ================================================================
    // 2️⃣ Upload via URL
    // ================================================================
    @Override
    public DatasetInformation uploadAndSchemaCheckByUrl(Long datasetTypeId, String fileUrl,
                                                        String name, String description) {
        log.info("[UPLOAD-URL] Starting schema validation from URL: {}", fileUrl);
        try {
            DatasetType type = datasetTypeRepo.findById(datasetTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("DatasetType not found: " + datasetTypeId));

            DatasetInformation ds = new DatasetInformation();
            ds.setName(name);
            ds.setFile_url(fileUrl);
            ds.setStatus(DatasetInforStatus.PENDING);
            ds = datasetInforRepository.save(ds);

            URL url = new URL(fileUrl);
            try (Reader r = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r);
                List<ValidationErrorDto> errors = validateSchema(type, parser.getHeaderMap().keySet(), parser.getRecords().size());

                if (!errors.isEmpty()) {
                    ds.setStatus(DatasetInforStatus.SCHEMA_FAILED);
                    ds.setValidationErrors(errors);
                    ds = datasetInforRepository.save(ds);
                    log.warn("[UPLOAD-URL] Invalid schema, {} errors found.", errors.size());
                } else {
                    ds.setStatus(DatasetInforStatus.PENDING_MODERATION);
                    ds.setRowCount((long) parser.getRecords().size());
                    ds = datasetInforRepository.save(ds);
                    log.info("[UPLOAD-URL] Schema validated successfully, pending moderation.");
                }
            }
            return ds;

        } catch (IOException e) {
            log.error("[UPLOAD-URL] Error reading file from URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read file from URL: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[UPLOAD-URL] Unexpected error: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during schema validation via URL: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public Map<String, Object> moderate(Long datasetId, Double thresholdPercent) {
        log.info("[MODERATE] Start moderation for dataset id={} threshold={}%", datasetId,
                thresholdPercent == null ? "default 2%" : thresholdPercent + "%");
        try {
            DatasetInformation ds = datasetInforRepository.findById(datasetId)
                    .orElseThrow(() -> new IllegalArgumentException("Dataset not found: " + datasetId));

            DatasetType type = ds.getDatasetType();
            if (type == null) {
                throw new IllegalStateException("Dataset type not found for dataset id=" + datasetId);
            }

            List<String> headers = type.getDatasetTypeColumnList()
                    .stream().map(DatasetTypeColumn::getColumnName).toList();

            // open CSV file
            try (InputStream in = openInputStream(ds.getFile_url());
                 Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                 CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(reader)) {

                List<CSVRecord> rows = parser.getRecords();
                List<String> csvHeaders = new ArrayList<>(parser.getHeaderMap().keySet());

                // detect numeric columns by name keywords
                List<String> numericCols = headers.stream()
                        .filter(h -> h.toLowerCase().matches(".*(kwh|power|sessions|price|frequency|time|monthly).*"))
                        .toList();

                List<DatasetValidationError> errors = new ArrayList<>();
                long totalCells = (long) rows.size() * csvHeaders.size();
                long totalErrors = 0;

                // null check
                for (int i = 0; i < rows.size(); i++) {
                    for (String col : csvHeaders) {
                        String value = safeGet(rows.get(i), col);
                        if (value == null || value.isBlank()) {
                            totalErrors++;
                            errors.add(DatasetValidationError.builder()
                                    .datasetInformation(ds)
                                    .validationPhase(ValidationPhase.MODERATION)
                                    .errorCode(ErrorCode.NULL_VALUE)
                                    .columnName(col)
                                    .rowIndex((long) i)
                                    .message("Null or blank value at row " + (i + 1) + ", column '" + col + "'")
                                    .build());
                        }
                    }
                }

                // duplicate check
                Set<String> seen = new HashSet<>();
                for (int i = 0; i < rows.size(); i++) {
                    int finalI = i;
                    String signature = csvHeaders.stream()
                            .map(h -> safeGet(rows.get(finalI), h))
                            .collect(Collectors.joining("|"));
                    if (!seen.add(signature)) {
                        totalErrors += csvHeaders.size();
                        errors.add(DatasetValidationError.builder()
                                .datasetInformation(ds)
                                .validationPhase(ValidationPhase.MODERATION)
                                .errorCode(ErrorCode.DUPLICATE_ROW)
                                .rowIndex((long) i)
                                .message("Row " + (i + 1) + " is a duplicate of a previous row")
                                .build());
                    }
                }

                // invalid number format check
                Map<String, List<Double>> numericValues = new HashMap<>();
                for (String col : numericCols) numericValues.put(col, new ArrayList<>());

                for (int i = 0; i < rows.size(); i++) {
                    for (String col : numericCols) {
                        String val = safeGet(rows.get(i), col);
                        if (val == null || val.isBlank()) continue;
                        try {
                            double num = Double.parseDouble(val);
                            numericValues.get(col).add(num);
                        } catch (NumberFormatException ex) {
                            totalErrors++;
                            errors.add(DatasetValidationError.builder()
                                    .datasetInformation(ds)
                                    .validationPhase(ValidationPhase.MODERATION)
                                    .errorCode(ErrorCode.INVALID_FORMAT)
                                    .columnName(col)
                                    .rowIndex((long) i)
                                    .message("Invalid numeric format at row " + (i + 1) + ", column '" + col + "': '" + val + "'")
                                    .build());
                        }
                    }
                }

                // outlier detection (IQR method)
                for (Map.Entry<String, List<Double>> entry : numericValues.entrySet()) {
                    String col = entry.getKey();
                    List<Double> vals = entry.getValue();
                    if (vals.size() < 5) continue;

                    Collections.sort(vals);
                    double q1 = vals.get(vals.size() / 4);
                    double q3 = vals.get(3 * vals.size() / 4);
                    double iqr = q3 - q1;
                    double lower = q1 - 1.5 * iqr;
                    double upper = q3 + 1.5 * iqr;

                    for (int i = 0; i < rows.size(); i++) {
                        String val = safeGet(rows.get(i), col);
                        if (val == null || val.isBlank()) continue;
                        try {
                            double num = Double.parseDouble(val);
                            if (num < lower || num > upper) {
                                totalErrors++;
                                errors.add(DatasetValidationError.builder()
                                        .datasetInformation(ds)
                                        .validationPhase(ValidationPhase.MODERATION)
                                        .errorCode(ErrorCode.OUT_OF_RANGE)
                                        .columnName(col)
                                        .rowIndex((long) i)
                                        .message(String.format(
                                                "Outlier at row %d, column '%s': %.2f (expected between %.2f and %.2f)",
                                                (i + 1), col, num, lower, upper))
                                        .build());
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }

                // save and result
                double rate = totalCells == 0 ? 0 : (100.0 * totalErrors / totalCells);
                boolean pass = rate <= (thresholdPercent == null ? 2.0 : thresholdPercent);
                ds.setStatus(pass ? DatasetInforStatus.APPROVED : DatasetInforStatus.REJECTED);
                datasetInforRepository.save(ds);

                errorRepository.deleteByDatasetInformation(ds);
                errorRepository.saveAll(errors);

                log.info("[MODERATE] Completed dataset id={}, errors={}, rate={}%, status={}",
                        datasetId, totalErrors, String.format("%.2f", rate), ds.getStatus());

                return Map.of(
                        "datasetId", ds.getId(),
                        "status", ds.getStatus(),
                        "errorRatePercent", rate,
                        "totalErrors", totalErrors,
                        "errors", errors.stream().map(e -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("code", e.getErrorCode() != null ? e.getErrorCode().name() : "UNKNOWN");
                            map.put("columnName", e.getColumnName() != null ? e.getColumnName() : "");
                            map.put("rowIndex", e.getRowIndex() != null ? e.getRowIndex() : -1);
                            map.put("message", e.getMessage() != null ? e.getMessage() : "");
                            return map;
                        }).toList()

                );

            } catch (IOException e) {
                log.error("[MODERATE] Error reading CSV file: {}", e.getMessage(), e);
                return Map.of("success", false, "message", "Cannot read CSV file");
            }

        } catch (Exception e) {
            log.error("[MODERATE] Unexpected error", e);
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    private List<ValidationErrorDto> validateSchema(DatasetType type, Set<String> provided, int recordCount) {
        List<ValidationErrorDto> errors = new ArrayList<>();
        Set<String> required = type.getDatasetTypeColumnList().stream()
                .map(c -> c.getColumnName().trim()).collect(Collectors.toSet());

        Set<String> missing = new HashSet<>(required);
        missing.removeAll(provided);
        Set<String> extra = new HashSet<>(provided);
        extra.removeAll(required);

        for (String c : missing) {
            errors.add(new ValidationErrorDto(ValidationPhase.SCHEMA_CHECK, ErrorCode.MISSING_COLUMN, c, null, "Missing column: " + c));
        }
        for (String c : extra) {
            errors.add(new ValidationErrorDto(ValidationPhase.SCHEMA_CHECK, ErrorCode.EXTRA_COLUMN, c, null, "Unexpected column: " + c));
        }

        return errors;
    }

    private String saveTemp(MultipartFile file) throws IOException {
        Path tempDir = Files.createTempDirectory("uploads");
        Path target = tempDir.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        log.info("[UPLOAD] Temporary file saved at: {}", target.toAbsolutePath());
        return target.toAbsolutePath().toString();
    }

    private InputStream openInputStream(String path) throws IOException {
        if (path == null) throw new FileNotFoundException("File path is null");
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return new URL(path).openStream();
        } else {
            return new FileInputStream(new File(path));
        }
    }

    private String safeGet(CSVRecord r, String c) {
        return r.isMapped(c) ? r.get(c) : null;
    }

    @Override
    public void saveErrors(DatasetInformation datasetInformation, List<DatasetValidationError> errors) {
        try {
            errorRepository.deleteByDatasetInformation(datasetInformation);
            errors.forEach(e -> e.setDatasetInformation(datasetInformation));
            errorRepository.saveAll(errors);
            log.info("[ERROR-SAVE] Saved {} moderation errors for dataset id={}", errors.size(), datasetInformation.getId());
        } catch (Exception e) {
            log.error("[ERROR-SAVE] Failed to save moderation errors: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save moderation errors to database", e);
        }
    }

    @Override
    public List<DatasetValidationError> getErrorsByDataset(DatasetInformation datasetInformation) {
        try {
            List<DatasetValidationError> errors = errorRepository.findByDatasetInformation(datasetInformation);
            log.info("[ERROR-GET] Found {} errors for dataset id={}", errors.size(), datasetInformation.getId());
            return errors;
        } catch (Exception e) {
            log.error("[ERROR-GET] Failed to query moderation errors: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve moderation errors", e);
        }
    }
}
