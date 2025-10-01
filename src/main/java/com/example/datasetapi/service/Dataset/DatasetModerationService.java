package com.example.datasetapi.service.dataset;

import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.enums.Datasets.ErrorCode;
import com.example.datasetapi.enums.Datasets.ValidationPhase;
import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetError;
import com.example.datasetapi.repository.DatasetErrorRepository;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatasetModerationService {

    private final DatasetRepository datasetRepo;
    private final DatasetErrorRepository errorRepo;

    private static final Set<String> CONNECTOR_ALLOWED = Set.of("CCS1","CCS2","CHAdeMO","Type2","GB/T");
    private static final Set<String> PRICING_MODEL_ALLOWED = Set.of("Flat","Time-based","Energy-based","Subscription");

    public Map<String,Object> moderate(Long datasetId, Double thresholdPercent) {
        try {
            Dataset ds = datasetRepo.findById(datasetId)
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay Dataset: " + datasetId));

            if (ds.getStatus() != DatasetStatus.PENDING_MODERATION) {
                return Map.of("message", "Dataset khong o trang thai PENDING_MODERATION", "status", ds.getStatus());
            }

            // 🔹 Chọn input stream dựa vào link hay local path
            try (InputStream in = openInputStream(ds.getFile_url());
                 Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {

                CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r);
                List<CSVRecord> rows = parser.getRecords();
                List<String> cols = new ArrayList<>(parser.getHeaderMap().keySet());

                long totalCells = (long) rows.size() * cols.size();
                long totalErrors = 0;

                // check null
                for (int i = 0; i < rows.size(); i++) {
                    for (String c : cols) {
                        if (rows.get(i).get(c) == null || rows.get(i).get(c).isBlank()) {
                            totalErrors++;
                            log(ds, ErrorCode.NULL_VALUE, c, (long) i, "Gia tri null/blank");
                        }
                    }
                }

                // check duplicate
                Set<String> sigs = new HashSet<>();
                for (int i = 0; i < rows.size(); i++) {
                    int finalI = i;
                    String sig = cols.stream().map(c -> rows.get(finalI).get(c)).collect(Collectors.joining("|"));
                    if (!sigs.add(sig)) {
                        totalErrors += cols.size();
                        log(ds, ErrorCode.DUPLICATE_ROW, null, (long) i, "Dong trung toan bo cot");
                    }
                }

                // check enum
                for (int i=0;i<rows.size();i++) {
                    String pm = safeGet(rows.get(i),"Pricing_Model");
                    if (pm!=null && !pm.isBlank() && !PRICING_MODEL_ALLOWED.contains(pm)) {
                        totalErrors++;
                        log(ds, ErrorCode.INVALID_ENUM, "Pricing_Model", (long)i, "Gia tri khong hop le: "+pm);
                    }
                    String ct = safeGet(rows.get(i),"Connector_Type");
                    if (ct!=null && !ct.isBlank() && !CONNECTOR_ALLOWED.contains(ct)) {
                        totalErrors++;
                        log(ds, ErrorCode.INVALID_ENUM, "Connector_Type", (long)i, "Gia tri khong hop le: "+ct);
                    }
                }

                double rate = totalCells==0?0:(100.0*totalErrors/totalCells);
                boolean pass = rate <= (thresholdPercent==null?2.0:thresholdPercent);

                ds.setStatus(pass?DatasetStatus.APPROVED:DatasetStatus.REJECTED);
                datasetRepo.save(ds);

                return Map.of(
                        "datasetId", ds.getId(),
                        "status", ds.getStatus(),
                        "errorRatePercent", rate,
                        "totalErrors", totalErrors,
                        "totalCells", totalCells
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Loi moderation: " + e.getMessage(), e);
        }
    }

    /**
     * Helper: mở stream từ local hoặc URL
     */
    private InputStream openInputStream(String path) throws IOException {
        if (path == null) throw new FileNotFoundException("File URL null");
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return new URL(path).openStream();
        } else {
            return new FileInputStream(new File(path));
        }
    }

    private String safeGet(CSVRecord r, String c) {
        return r.isMapped(c)?r.get(c):null;
    }

    private void log(Dataset ds, ErrorCode code, String col, Long row, String msg) {
        DatasetError e = new DatasetError();
        e.setDataset(ds);
        e.setPhase(ValidationPhase.MODERATION);
        e.setCode(code);
        e.setColumnName(col);
        e.setRowIndex(row);
        e.setMessage(msg);
        errorRepo.save(e);
    }
}
