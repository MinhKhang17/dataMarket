package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.ValidationErrorDto;
import com.example.datasetapi.enums.Datasets.*;
import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.model.UserManager.Provider;
import com.example.datasetapi.repository.DatasetInforRepository;
import com.example.datasetapi.repository.DatasetTypeRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;



@Service
public class DatasetValidateServiceImpl implements DatasetValidateService {

    private static final Set<String> CONNECTOR_ALLOWED = Set.of("CCS1", "CCS2", "CHAdeMO", "Type2", "GB/T");
    private static final Set<String> PRICING_MODEL_ALLOWED = Set.of("Flat", "Time-based", "Energy-based", "Subscription");

    @Autowired
    private  DatasetTypeRepository datasetTypeRepo;
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

    @Override
    public ResponseEntity<?> uploadAndHeaderCheckCSVFile(Long datasetTypeId, MultipartFile file, String name, String description, HttpServletRequest request) {
        try {
            String token = tokenService.resolveToken(request);
            long provider_id = jwtUtil.getUserIdFromToken(token);

            Provider provider = userService.findProviderById(provider_id);

            DatasetType type = datasetTypeRepo.findById(datasetTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy DatasetType: " + datasetTypeId));

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
            ds.setDatasetExtension(FileExtension.valueOf(extension));
            ds = datasetInforRepository.save(ds);

            try (Reader r = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
                CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r);

                List<ValidationErrorDto> errors = validateSchema(type, parser.getHeaderMap().keySet(), parser.getRecords().size());

                if (!errors.isEmpty()) {
                    ds.setStatus(DatasetInforStatus.SCHEMA_FAILED);
                    ds.setValidationErrors(errors);
                    ds = datasetInforRepository.save(ds);
                } else {
                    ds.setStatus(DatasetInforStatus.PENDING_MODERATION);
                    ds.setRowCount((long) parser.getRecords().size());
                    ds = datasetInforRepository.save(ds);
                    //neu khong loi thi upload len cloud
                    datasetService.uploadCSVFileToPendingFolder(file,dataset);
                }
            }
            return ResponseEntity.ok().body(new ApiResponse(true,"check success",ds));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload schema: " + e.getMessage(), e);
        }
    }

    @Override
    public DatasetInformation uploadAndSchemaCheckByUrl(Long datasetTypeId, String fileUrl, String name, String description) {
        try {
            DatasetType type = datasetTypeRepo.findById(datasetTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy DatasetType: " + datasetTypeId));

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
                } else {
                    ds.setStatus(DatasetInforStatus.PENDING_MODERATION);
                    ds.setRowCount((long) parser.getRecords().size());
                    ds = datasetInforRepository.save(ds);
                }
            }
            return ds;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload schema bằng URL: " + e.getMessage(), e);
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
            errors.add(new ValidationErrorDto(ValidationPhase.SCHEMA_CHECK, ErrorCode.MISSING_COLUMN, c, null, "Thiếu cột: " + c));
        }
        for (String c : extra) {
            errors.add(new ValidationErrorDto(ValidationPhase.SCHEMA_CHECK, ErrorCode.EXTRA_COLUMN, c, null, "Thừa cột: " + c));
        }

        return errors;
    }

    private String saveTemp(MultipartFile file) {
        try {
            Path tempDir = Files.createTempDirectory("uploads");
            Path target = tempDir.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu file tạm: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> moderate(Long datasetId, Double thresholdPercent) {
        try {
            DatasetInformation ds = datasetInforRepository.findById(datasetId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Dataset: " + datasetId));

            if (ds.getStatus() != DatasetInforStatus.PENDING_MODERATION) {
                return Map.of(
                        "message", "Dataset không ở trạng thái PENDING_MODERATION",
                        "status", ds.getStatus()
                );
            }

            //Mở file local hoặc URL
            try (InputStream in = openInputStream(ds.getFile_url());
                 Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {

                CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r);
                List<CSVRecord> rows = parser.getRecords();
                List<String> cols = new ArrayList<>(parser.getHeaderMap().keySet());

                long totalCells = (long) rows.size() * cols.size();
                long totalErrors = 0;

                List<ValidationErrorDto> errors = new ArrayList<>();

                // 🔹 Check null/blank
                for (int i = 0; i < rows.size(); i++) {
                    for (String c : cols) {
                        if (rows.get(i).get(c) == null || rows.get(i).get(c).isBlank()) {
                            totalErrors++;
                            errors.add(new ValidationErrorDto(
                                    ValidationPhase.MODERATION,
                                    ErrorCode.NULL_VALUE,
                                    c,
                                    (long) i,
                                    "Giá trị null/blank"
                            ));
                        }
                    }
                }

                // 🔹 Check duplicate row
                Set<String> sigs = new HashSet<>();
                for (int i = 0; i < rows.size(); i++) {
                    int finalI = i;
                    String sig = cols.stream().map(c -> rows.get(finalI).get(c)).collect(Collectors.joining("|"));
                    if (!sigs.add(sig)) {
                        totalErrors += cols.size();
                        errors.add(new ValidationErrorDto(
                                ValidationPhase.MODERATION,
                                ErrorCode.DUPLICATE_ROW,
                                null,
                                (long) i,
                                "Dòng trùng toàn bộ cột"
                        ));
                    }
                }

                // 🔹 Check enum
                for (int i = 0; i < rows.size(); i++) {
                    String pm = safeGet(rows.get(i), "Pricing_Model");
                    if (pm != null && !pm.isBlank() && !PRICING_MODEL_ALLOWED.contains(pm)) {
                        totalErrors++;
                        errors.add(new ValidationErrorDto(
                                ValidationPhase.MODERATION,
                                ErrorCode.INVALID_ENUM,
                                "Pricing_Model",
                                (long) i,
                                "Giá trị không hợp lệ: " + pm
                        ));
                    }
                    String ct = safeGet(rows.get(i), "Connector_Type");
                    if (ct != null && !ct.isBlank() && !CONNECTOR_ALLOWED.contains(ct)) {
                        totalErrors++;
                        errors.add(new ValidationErrorDto(
                                ValidationPhase.MODERATION,
                                ErrorCode.INVALID_ENUM,
                                "Connector_Type",
                                (long) i,
                                "Giá trị không hợp lệ: " + ct
                        ));
                    }
                }

                // 🔹 Tính error rate
                double rate = totalCells == 0 ? 0 : (100.0 * totalErrors / totalCells);
                boolean pass = rate <= (thresholdPercent == null ? 2.0 : thresholdPercent);

                ds.setStatus(pass ? DatasetInforStatus.APPROVED : DatasetInforStatus.REJECTED);
                datasetInforRepository.save(ds);

                return Map.of(
                        "datasetId", ds.getId(),
                        "status", ds.getStatus(),
                        "errorRatePercent", rate,
                        "totalErrors", totalErrors,
                        "totalCells", totalCells,
                        "errors", errors   // Trả list lỗi ra JSON
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi moderation: " + e.getMessage(), e);
        }
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
