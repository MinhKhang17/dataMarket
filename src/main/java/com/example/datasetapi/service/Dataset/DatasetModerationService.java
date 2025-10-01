package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.response.ValidationErrorDto;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.enums.Datasets.ErrorCode;
import com.example.datasetapi.enums.Datasets.ValidationPhase;
import com.example.datasetapi.model.Dataset.DatasetInfor;
import com.example.datasetapi.repository.DatasetRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatasetModerationService {

    private final DatasetRepository datasetRepo;

    private static final Set<String> CONNECTOR_ALLOWED = Set.of("CCS1", "CCS2", "CHAdeMO", "Type2", "GB/T");
    private static final Set<String> PRICING_MODEL_ALLOWED = Set.of("Flat", "Time-based", "Energy-based", "Subscription");

    public Map<String, Object> moderate(Long datasetId, Double thresholdPercent) {
        try {
            DatasetInfor ds = datasetRepo.findById(datasetId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Dataset: " + datasetId));

            if (ds.getStatus() != DatasetStatus.PENDING_MODERATION) {
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

                ds.setStatus(pass ? DatasetStatus.APPROVED : DatasetStatus.REJECTED);
                datasetRepo.save(ds);

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
