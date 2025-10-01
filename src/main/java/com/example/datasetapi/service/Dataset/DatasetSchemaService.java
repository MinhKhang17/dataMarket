package com.example.datasetapi.service.dataset;

import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.enums.Datasets.ErrorCode;
import com.example.datasetapi.enums.Datasets.ValidationPhase;
import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetError;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.repository.DatasetErrorRepository;
import com.example.datasetapi.repository.DatasetRepository;
import com.example.datasetapi.repository.DatasetTypeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
    private final DatasetErrorRepository errorRepo;

    /**
     * Upload local file và kiểm tra schema (multi datasetTypeIds)
     */
    public Dataset uploadAndSchemaCheckMulti(List<Long> datasetTypeIds,
                                             MultipartFile file,
                                             String name,
                                             String description) {
        try {
            List<DatasetType> types = datasetTypeRepo.findAllById(datasetTypeIds);
            if (types.isEmpty()) {
                throw new IllegalArgumentException("Khong tim thay dataset types: " + datasetTypeIds);
            }

            // Lưu file tạm local
            String path = saveTemp(file);

            Dataset ds = new Dataset();
            ds.setName(name);
            ds.setDescription(description);
            ds.setDatasetTypeList(types);  // gán list thay vì 1-1
            ds.setFile_url(path);
            ds.setStatus(DatasetStatus.PENDING);
            ds = datasetRepo.save(ds);

            // Kiểm tra schema theo type đầu tiên (hoặc merge logic nếu cần)
            try (Reader r = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
                CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r);
                validateSchema(ds, types.get(0), parser.getHeaderMap().keySet(), parser.getRecords().size());
            }

            return datasetRepo.save(ds);
        } catch (Exception e) {
            System.out.println("Loi khi upload va kiem tra schema multi: " + e.getMessage());
            throw new RuntimeException("Loi upload schema multi", e);
        }
    }

    /**
     * Upload bằng URL và kiểm tra schema (1 datasetTypeId)
     */
    public Dataset uploadAndSchemaCheckByUrl(Long datasetTypeId,
                                             String fileUrl,
                                             String name,
                                             String description) {
        try {
            DatasetType type = datasetTypeRepo.findById(datasetTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay DatasetType: " + datasetTypeId));

            Dataset ds = new Dataset();
            ds.setName(name);
            ds.setDescription(description);
            ds.setDatasetTypeList(Collections.singletonList(type)); // gán vào list
            ds.setFile_url(fileUrl);
            ds.setStatus(DatasetStatus.PENDING);
            ds = datasetRepo.save(ds);

            URL url = new URL(fileUrl);
            try (InputStream in = url.openStream();
                 Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r);
                validateSchema(ds, type, parser.getHeaderMap().keySet(), parser.getRecords().size());
            }

            return datasetRepo.save(ds);
        } catch (Exception e) {
            System.out.println("Loi khi upload bang URL va kiem tra schema: " + e.getMessage());
            throw new RuntimeException("Loi upload schema bang URL", e);
        }
    }

    /**
     * Validate schema của dataset
     */
    private void validateSchema(Dataset ds, DatasetType type, Set<String> provided, int recordCount) {
        Set<String> required = type.getDatasetTypeColumnList().stream()
                .map(c -> c.getColumnName().trim())
                .collect(Collectors.toSet());

        Set<String> missing = new HashSet<>(required);
        missing.removeAll(provided);

        Set<String> extra = new HashSet<>(provided);
        extra.removeAll(required);

        if (!missing.isEmpty() || !extra.isEmpty()) {
            for (String c : missing) {
                logError(ds, ValidationPhase.SCHEMA_CHECK, ErrorCode.MISSING_COLUMN, c, null, "Thieu cot: " + c);
            }
            for (String c : extra) {
                logError(ds, ValidationPhase.SCHEMA_CHECK, ErrorCode.EXTRA_COLUMN, c, null, "Thua cot: " + c);
            }
            ds.setStatus(DatasetStatus.SCHEMA_FAILED);
        } else {
            ds.setStatus(DatasetStatus.PENDING_MODERATION);
            ds.setRowCount((long) recordCount);
        }
    }

    /**
     * Ghi log lỗi vào DatasetError
     */
    private void logError(Dataset ds, ValidationPhase phase, ErrorCode code, String col, Long row, String msg) {
        DatasetError e = new DatasetError();
        e.setDataset(ds);
        e.setPhase(phase);
        e.setCode(code);
        e.setColumnName(col);
        e.setRowIndex(row);
        e.setMessage(msg);
        errorRepo.save(e);
    }

    /**
     * Lưu file tạm (local)
     */
    private String saveTemp(MultipartFile file) {
        try {
            Path tempDir = Files.createTempDirectory("uploads");
            Path target = tempDir.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toUri().toString(); // trả về file://... để local đọc được
        } catch (IOException e) {
            throw new RuntimeException("Loi luu file tam: " + e.getMessage(), e);
        }
    }

    public Dataset uploadAndSchemaCheckByUrlMulti(List<Long> datasetTypeIds,
                                                  String fileUrl,
                                                  String name,
                                                  String description) {
        try {
            List<DatasetType> types = datasetTypeRepo.findAllById(datasetTypeIds);
            if (types.isEmpty()) {
                throw new IllegalArgumentException("Khong tim thay dataset types: " + datasetTypeIds);
            }

            Dataset ds = new Dataset();
            ds.setName(name);
            ds.setDescription(description);
            ds.setDatasetTypeList(types); // gán list thay vì 1-1
            ds.setFile_url(fileUrl);
            ds.setStatus(DatasetStatus.PENDING);
            ds = datasetRepo.save(ds);

            URL url = new URL(fileUrl);
            try (InputStream in = url.openStream();
                 Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r);

                // validate schema theo type đầu tiên (hoặc bạn merge nhiều type)
                validateSchema(ds, types.get(0), parser.getHeaderMap().keySet(), parser.getRecords().size());
            }

            return datasetRepo.save(ds);
        } catch (Exception e) {
            System.out.println("Loi khi upload URL multi: " + e.getMessage());
            throw new RuntimeException("Loi upload schema multi URL", e);
        }
    }

}
