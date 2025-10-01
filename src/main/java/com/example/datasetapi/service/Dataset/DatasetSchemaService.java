package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.response.ValidationErrorDto;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.enums.Datasets.ErrorCode;
import com.example.datasetapi.enums.Datasets.ValidationPhase;
import com.example.datasetapi.model.Dataset.DatasetInfor;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.repository.DatasetRepository;
import com.example.datasetapi.repository.DatasetTypeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatasetSchemaService {

    private final DatasetTypeRepository datasetTypeRepo;
    private final DatasetRepository datasetRepo;

    public DatasetInfor uploadAndSchemaCheck(Long datasetTypeId,
                                             MultipartFile file,
                                             String name,
                                             String description) {
        try {
            DatasetType type = datasetTypeRepo.findById(datasetTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy DatasetType: " + datasetTypeId));

            String path = saveTemp(file);

            DatasetInfor ds = new DatasetInfor();
            ds.setName(name);
            ds.setDescription(description);
            ds.setDatasetType(type);
            ds.setFile_url(path);
            ds.setStatus(DatasetStatus.PENDING);
            ds = datasetRepo.save(ds);

            try (Reader r = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
                CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r);

                List<ValidationErrorDto> errors = validateSchema(type, parser.getHeaderMap().keySet(), parser.getRecords().size());

                if (!errors.isEmpty()) {
                    ds.setStatus(DatasetStatus.SCHEMA_FAILED);
                    ds.setValidationErrors(errors);
                    ds = datasetRepo.save(ds);
                } else {
                    ds.setStatus(DatasetStatus.PENDING_MODERATION);
                    ds.setRowCount((long) parser.getRecords().size());
                    ds = datasetRepo.save(ds);                 }
            }
            return ds;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload schema: " + e.getMessage(), e);
        }
    }

    public DatasetInfor uploadAndSchemaCheckByUrl(Long datasetTypeId,
                                                  String fileUrl,
                                                  String name,
                                                  String description) {
        try {
            DatasetType type = datasetTypeRepo.findById(datasetTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy DatasetType: " + datasetTypeId));

            DatasetInfor ds = new DatasetInfor();
            ds.setName(name);
            ds.setDescription(description);
            ds.setDatasetType(type);
            ds.setFile_url(fileUrl);
            ds.setStatus(DatasetStatus.PENDING);
            ds = datasetRepo.save(ds);

            URL url = new URL(fileUrl);
            try (Reader r = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r);
                List<ValidationErrorDto> errors = validateSchema(type, parser.getHeaderMap().keySet(), parser.getRecords().size());

                if (!errors.isEmpty()) {
                    ds.setStatus(DatasetStatus.SCHEMA_FAILED);
                    ds.setValidationErrors(errors);
                    ds = datasetRepo.save(ds);
                } else {
                    ds.setStatus(DatasetStatus.PENDING_MODERATION);
                    ds.setRowCount((long) parser.getRecords().size());
                    ds = datasetRepo.save(ds);
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
}
