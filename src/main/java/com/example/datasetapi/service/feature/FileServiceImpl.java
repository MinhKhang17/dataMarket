package com.example.datasetapi.service.feature;

import com.example.datasetapi.dto.response.ValidationErrorDto;
import com.example.datasetapi.enums.Datasets.DatasetInforStatus;
import com.example.datasetapi.enums.Datasets.ErrorCode;
import com.example.datasetapi.enums.Datasets.FileExtension;
import com.example.datasetapi.enums.Datasets.ValidationPhase;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.model.Dataset.DatasetTypeColumn;
import com.example.datasetapi.model.Dataset.DatasetValidationError;
import com.example.datasetapi.model.UserManager.Provider;
import com.example.datasetapi.repository.DatasetInforRepository;
import com.example.datasetapi.repository.DatasetTypeRepository;
import com.example.datasetapi.repository.DatasetValidationErrorRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;



import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private DatasetTypeRepository datasetTypeRepository;
    @Autowired
    private DatasetInforRepository datasetInforRepository;
    @Autowired
    private  DatasetValidationErrorRepository errorRepository;

    private final double THRE_HOLD_PERCENT = 2.0;



    @Override
    public boolean checkHeader(MultipartFile file, long datasetTypeId, DatasetInformation ds, Provider provider) {
        try{
            System.out.println("-----------------------------------------\n" +
                    "Start reading Dataset\n" +
                    "-----------------------------------------");
            DatasetType type = datasetTypeRepository.findById(datasetTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("DatasetType not found:" + datasetTypeId));

            String path = saveTemp(file);

            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }

//          Dataset dataset = new Dataset();
//         dataset.setDescription(providerUploadDatasetRequest.getDescription());
//
//         ds.setName(providerUploadDatasetRequest.getTitle());

            ds.setFile_url(path);
            ds.setStatus(DatasetInforStatus.PENDING);
            ds.setDatasetExtension(FileExtension.valueOf(extension));
            ds.setHeaderChecked(true);
            ds.setProvider(provider);
            ds.setDatasetType(type);
            ds.setUpdateAt(LocalDateTime.now());
            ds = datasetInforRepository.save(ds);

            try (Reader r = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
                CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r);

                Set<String> headers = parser.getHeaderMap().keySet();
                List<CSVRecord> recordList = parser.getRecords();
                int rowCount = recordList.size();

                List<ValidationErrorDto> errors = validateSchema(type, parser.getHeaderMap().keySet(), parser.getRecords().size());

                if (!errors.isEmpty()) {
                    ds.setStatus(DatasetInforStatus.SCHEMA_FAILED);
                    ds.setValidationErrors(errors);
                    ds = datasetInforRepository.save(ds);
                    return false;
                } else {
                    ds.setStatus(DatasetInforStatus.PENDING_MODERATION);
                    ds.setRowCount((long) rowCount);
                    ds = datasetInforRepository.save(ds);

                    return true;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            finally {
//                if (path != null) {
//                    try {
//
//                        System.out.println("-----------------------------------------\n" +
//                                "Da xoa dataset\n" +
//                                "-----------------------------------------");
//                        Files.deleteIfExists(Paths.get(path));
//                    } catch (IOException e) {
//                        System.err.println(" Không thể xóa file tạm: " + e.getMessage());
//                    }
//                }
//            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DatasetInformation uploadAndSchemaCheckByUrl(Long datasetTypeId, String fileUrl, String name, String description) {
        try {
            DatasetType type = datasetTypeRepository.findById(datasetTypeId)
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
                    datasetInforRepository.delete(ds);
                } else {
                    ds.setStatus(DatasetInforStatus.PENDING_MODERATION);
                    ds.setRowCount((long) parser.getRecords().size());
                    ds = datasetInforRepository.save(ds);
                }
            }
            return ds;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read file from URL: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during schema validation via URL: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveErrors(DatasetInformation datasetInformation, List<DatasetValidationError> errors) {
        try {
            errorRepository.deleteByDatasetInformation(datasetInformation);
            errors.forEach(e -> e.setDatasetInformation(datasetInformation));
            errorRepository.saveAll(errors);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save moderation errors to database", e);
        }
    }

    @Override
    public List<DatasetValidationError> getErrorsByDataset(DatasetInformation datasetInformation) {
        try {
            List<DatasetValidationError> errors = errorRepository.findByDatasetInformation(datasetInformation);
            return errors;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve moderation errors", e);
        }
    }

    @Override
    public Map<String, Object> moderate(DatasetInformation ds, DatasetType datasetType) {

        try {


            if(!ds.isHeaderChecked()){
                throw new IllegalStateException("Dataset is not checked header for dataset id=" + ds.getId());
            }



            List<String> headers = datasetType.getDatasetTypeColumnList()
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
                boolean pass = rate <= THRE_HOLD_PERCENT;
                if (!pass) {
                    datasetInforRepository.deleteById(ds.getId());
                }
                else {
                    ds.setStatus(DatasetInforStatus.CONTENT_APPROVED);
                    ds.setContentChecked(true);
                    datasetInforRepository.save(ds);

                    errorRepository.deleteByDatasetInformation(ds);
                    errorRepository.saveAll(errors);
                }


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
                return Map.of("success", false, "message", "Cannot read CSV file");
            }

        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }finally {
            try {

                System.out.println("-----------------------------------------\n" +
                        "Deleted dataset\n" +
                        "-----------------------------------------");
                Files.deleteIfExists(Paths.get(ds.getFile_url()));
            } catch (IOException e) {
                System.err.println("Can not delete current file: " + e.getMessage());
            }
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
        return target.toAbsolutePath().toString();
    }

    private InputStream openInputStream(String path) throws IOException {
        if (path == null) throw new FileNotFoundException("File URL null");
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return new URL(path).openStream();
        } else {
            return new FileInputStream(new File(path));
        }
    }

    private String safeGet(CSVRecord r, String c) {
        return r.isMapped(c) ? r.get(c) : null;
    }




}
