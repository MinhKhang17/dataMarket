package com.example.datasetapi.service.feature;

import com.example.datasetapi.dto.response.ValidationErrorDto;
import com.example.datasetapi.enums.Datasets.DatasetInforStatus;
import com.example.datasetapi.enums.Datasets.ErrorCode;
import com.example.datasetapi.enums.Datasets.FileExtension;
import com.example.datasetapi.enums.Datasets.ValidationPhase;
import com.example.datasetapi.model.dataset.DatasetInformation;
import com.example.datasetapi.model.dataset.DatasetType;
import com.example.datasetapi.model.dataset.DatasetTypeColumn;
import com.example.datasetapi.model.dataset.DatasetValidationError;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.DatasetInforRepository;
import com.example.datasetapi.repository.DatasetTypeRepository;
import com.example.datasetapi.repository.DatasetValidationErrorRepository;
import com.example.datasetapi.service.dataset.AnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.List;

/**
 * Chỉnh sửa để:
 *  - sau khi build prompt sẽ gọi LLM đồng bộ (llmClient.call)
 *  - validate JSON trả về từ LLM
 *  - lưu coreMetrics, prompt, aiResponse, sampleCsv, errors vào DB qua analysisService
 */
@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private DatasetTypeRepository datasetTypeRepository;
    @Autowired
    private DatasetInforRepository datasetInforRepository;
    @Autowired
    private DatasetValidationErrorRepository errorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // service để lưu analysis artifacts (entities + metrics + prompt)
    @Autowired
    private AnalysisService analysisService;

    // NEW: AiService (wraps ChatClient/Gemini)
    @Autowired
    private AiService aiService;

    private final double THRE_HOLD_PERCENT = 2.0;
    // max chars to store for ai raw response in DB (avoid oversized payload)
    private final int MAX_AI_SAVE_CHARS = 20000;
    // limit saving moderation errors to avoid DB spam
    private final int MAX_ERRORS_TO_SAVE = 1000;

    @Override
    public boolean checkHeader(MultipartFile file, long datasetTypeId, DatasetInformation ds, User provider) {
        String tempPath = null;
        try{
            log.info("-----------------------------------------\nStart reading Dataset\n-----------------------------------------");
            DatasetType type = datasetTypeRepository.findById(datasetTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("DatasetType not found:" + datasetTypeId));

            tempPath = saveTemp(file);

            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }

            ds.setFile_url(tempPath);
            ds.setStatus(DatasetInforStatus.PENDING);
            try {
                ds.setDatasetExtension(FileExtension.valueOf(extension));
            } catch (Exception ex) {
                // ignore if extension unknown
            }
            // do not set headerChecked here — wait until schema validated
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
                    ds.setHeaderChecked(false);
                    ds = datasetInforRepository.save(ds);
                    return false;
                } else {
                    ds.setStatus(DatasetInforStatus.PENDING_MODERATION);
                    ds.setHeaderChecked(true);
                    ds.setRowCount((long) rowCount);
                    ds = datasetInforRepository.save(ds);

                    return true;
                }
            } catch (IOException e) {
                log.error("IOException while parsing uploaded file", e);
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            log.error("checkHeader failed", e);
            throw e;
        } catch (IOException e) {
            log.error("checkHeader IO failed", e);
            throw new RuntimeException(e);
        } finally {
            // keep tempPath for later moderation; if saving temp only for header check you might delete here.
            // currently we keep the file and store path in ds.file_url
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
                    ds.setHeaderChecked(false);
                    datasetInforRepository.save(ds);
                } else {
                    ds.setStatus(DatasetInforStatus.PENDING_MODERATION);
                    ds.setHeaderChecked(true);
                    ds.setRowCount((long) parser.getRecords().size());
                    ds = datasetInforRepository.save(ds);
                }
            }
            return ds;

        } catch (IOException e) {
            log.error("Failed to read file from URL", e);
            throw new RuntimeException("Failed to read file from URL: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during schema validation via URL", e);
            throw new RuntimeException("Unexpected error during schema validation via URL: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveErrors(DatasetInformation datasetInformation, List<DatasetValidationError> errors) {
        try {
            errorRepository.deleteByDatasetInformation(datasetInformation);
            errors.forEach(e -> e.setDatasetInformation(datasetInformation));
            List<DatasetValidationError> toSave = errors.size() > MAX_ERRORS_TO_SAVE ? errors.subList(0, MAX_ERRORS_TO_SAVE) : errors;
            errorRepository.saveAll(toSave);
        } catch (Exception e) {
            log.error("Failed to save moderation errors to database", e);
            throw new RuntimeException("Failed to save moderation errors to database", e);
        }
    }

    @Override
    public List<DatasetValidationError> getErrorsByDataset(DatasetInformation datasetInformation) {
        try {
            List<DatasetValidationError> errors = errorRepository.findByDatasetInformation(datasetInformation);
            return errors;
        } catch (Exception e) {
            log.error("Failed to retrieve moderation errors", e);
            throw new RuntimeException("Failed to retrieve moderation errors", e);
        }
    }

    /**
     * Main moderation flow:
     * - đọc toàn bộ file (nếu > large file bạn có thể chuyển sang streaming)
     * - thực hiện validation cũ
     * - TÍNH core metrics theo dataset type (3 type)
     * - soạn prompt (tiếng Việt) + in prompt & metrics JSON ra console để bạn xem
     * - Gọi AiService để nhận ai result (raw + jsonNode)
     * - Lưu artifacts qua AnalysisService
     */
    @Override
    public Map<String, Object> moderate(DatasetInformation ds) {

        try {

            if(!ds.isHeaderChecked()){
                log.warn("Moderation called but header not checked for dataset id={}", ds.getId());
                return Map.of("success", false, "message", "The dataset hasn't been checked for headers.");
            }

            List<String> headers = ds.getDatasetType().getDatasetTypeColumnList()
                    .stream().map(DatasetTypeColumn::getColumnName).toList();

            // open CSV file
            try (InputStream in = openInputStream(ds.getFile_url());
                 Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                 CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(reader)) {

                List<CSVRecord> rows = parser.getRecords();
                List<String> csvHeaders = new ArrayList<>(parser.getHeaderMap().keySet());

                // --- existing validations (unchanged) ---
                List<String> numericCols = headers.stream()
                        .filter(h -> h.toLowerCase().matches(".*(kwh|power|sessions|price|frequency|time|monthly|duration|energy|soc|capacity).*"))
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
                            if (errors.size() >= MAX_ERRORS_TO_SAVE) break;
                        }
                    }
                    if (errors.size() >= MAX_ERRORS_TO_SAVE) break;
                }

                // duplicate check
                if (errors.size() < MAX_ERRORS_TO_SAVE) {
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
                            if (errors.size() >= MAX_ERRORS_TO_SAVE) break;
                        }
                    }
                }

                // invalid number format check
                Map<String, List<Double>> numericValues = new HashMap<>();
                for (String col : numericCols) numericValues.put(col, new ArrayList<>());

                for (int i = 0; i < rows.size(); i++) {
                    for (String col : numericCols) {
                        // Skip datetime columns
                        if (col == null) continue;
                        if (col.toLowerCase().contains("time") || col.toLowerCase().contains("date")) continue;

                        String val = safeGet(rows.get(i), col);
                        if (val == null || val.isBlank()) continue;
                        try {
                            double num = Double.parseDouble(val);
                            List<Double> list = numericValues.get(col);
                            if (list != null) list.add(num);
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
                            if (errors.size() >= MAX_ERRORS_TO_SAVE) break;
                        }
                    }
                    if (errors.size() >= MAX_ERRORS_TO_SAVE) break;
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
                                if (errors.size() >= MAX_ERRORS_TO_SAVE) break;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    if (errors.size() >= MAX_ERRORS_TO_SAVE) break;
                }

                // --- end existing validations ---

                // compute basic pass/fail
                double rate = totalCells == 0 ? 0 : (100.0 * totalErrors / totalCells);
                boolean pass = rate <= THRE_HOLD_PERCENT;

                // --- NEW: compute core metrics per dataset type ---
                Map<String, Object> coreMetrics = computeCoreMetricsByType(ds.getDatasetType().getName(), rows, csvHeaders);

                // create sample for prompt (use first up to 100 rows)
                int sampleSize = Math.min(100, rows.size());
                List<CSVRecord> sampleRows = rows.subList(0, sampleSize);
                String metricsJson = objectMapper.writeValueAsString(coreMetrics);
                String prompt = buildPromptForDatasetType(ds.getDatasetType(), sampleRows, csvHeaders, metricsJson);
                String sampleCsv = sampleToCsv(sampleRows, csvHeaders, 50);

                // print prompt + metrics JSON to console for review (yêu cầu của bạn)
                log.info("======= AI PROMPT (preview) =======\n{}", prompt);
                log.info("======= CORE METRICS JSON =======\n{}", metricsJson);
                log.info("======= SAMPLE CSV PREVIEW =======\n{}", sampleCsv);
                log.info("======= END PREVIEW =======");

                // --- NEW: call AiService to execute LLM (Gemini) or only print prompt if disabled ---
                String aiRawResponse = null;
                JsonNode aiJsonNode = null;
                try {
                    AiService.AiResult aiResult = aiService.callAnalysis(prompt, ds.getDatasetType().getName());
                    if (aiResult != null) {
                        aiRawResponse = aiResult.getRawResponse();
                        aiJsonNode = aiResult.getJsonResponse();
                    }
                } catch (Exception ex) {
                    // log and continue; do not abort the moderation flow
                    log.error("AI call failed", ex);
                }

                // fallback: if jsonResponse null try parse raw text
                if (aiJsonNode == null && aiRawResponse != null) {
                    try {
                        aiJsonNode = parseAiRawToJson(aiRawResponse);
                    } catch (Exception e) {
                        log.error("Failed to parse aiRawResponse to JSON", e);
                        aiJsonNode = null;
                    }
                }

                // validate structure of AI JSON response
                boolean aiValid = validateAiSchema(ds.getDatasetType().getName(), aiJsonNode);
                if (!aiValid) {
                    log.warn("AI returned invalid JSON schema for datasetType={}", ds.getDatasetType().getName());
                    // keep aiJsonNode = null, but keep raw response for debugging (truncated)
                    aiJsonNode = null;
                }

                // continue previous save / delete logic
                if (!pass) {
                    datasetInforRepository.deleteById(ds.getId());
                }
                else {
                    ds.setStatus(DatasetInforStatus.CONTENT_APPROVED);
                    ds.setContentChecked(true);
                    datasetInforRepository.save(ds);

                    // save validation errors (moderation)
                    try {
                        errorRepository.deleteByDatasetInformation(ds);
                        List<DatasetValidationError> toSave = errors.size() > MAX_ERRORS_TO_SAVE ? errors.subList(0, MAX_ERRORS_TO_SAVE) : errors;
                        toSave.forEach(e -> e.setDatasetInformation(ds));
                        errorRepository.saveAll(toSave);
                    } catch (Exception ex) {
                        log.error("Failed to save moderation errors", ex);
                    }

                    // --- NEW: persist analysis artifacts (coreMetrics, prompt, aiResponse, sampleCsv, errors) ---
                    String aiResponseToSave = aiRawResponse == null ? null : truncateString(aiRawResponse, MAX_AI_SAVE_CHARS);
                    try {
                        // Ensure AnalysisService interface matches this signature.
                        analysisService.saveAnalysisAndArtifacts(ds, coreMetrics, prompt, aiResponseToSave, sampleCsv, errors, rate, totalErrors);
                    } catch (Exception saveEx) {
                        log.error("Failed to save analysis artifacts", saveEx);
                        // we don't abort the endpoint — just log
                    }
                }

                // return response (include coreMetrics + previous info + ai response if any)
                Map<String, Object> response = new HashMap<>();
                response.put("datasetId", ds.getId());
                response.put("status", ds.getStatus());
                response.put("errorRatePercent", rate);
                response.put("totalErrors", totalErrors);
                response.put("errors", errors.stream().map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", e.getErrorCode() != null ? e.getErrorCode().name() : "UNKNOWN");
                    map.put("columnName", e.getColumnName() != null ? e.getColumnName() : "");
                    map.put("rowIndex", e.getRowIndex() != null ? e.getRowIndex() : -1);
                    map.put("message", e.getMessage() != null ? e.getMessage() : "");
                    return map;
                }).toList());
                response.put("coreMetrics", coreMetrics);
                response.put("aiPromptPreview", prompt);
                response.put("sampleCsv", sampleCsv);

                response.put("aiRawResponse", aiRawResponse == null ? null : truncateString(aiRawResponse, MAX_AI_SAVE_CHARS));
                if (aiJsonNode != null && aiValid) {
                    response.put("aiResponseJson", objectMapper.convertValue(aiJsonNode, Map.class));
                } else {
                    response.put("aiResponseJson", null);
                }

                return response;

            } catch (IOException e) {
                log.error("Cannot read CSV file", e);
                return Map.of("success", false, "message", "Cannot read CSV file");
            }

        } catch (Exception e) {
            log.error("Moderation failed", e);
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    // -------------------- HELPERS --------------------
    // (computeCoreMetricsByType, computeStationEnergyMetrics, computeTransactionBillingMetrics,
    // computeVehicleMetrics, buildPromptForDatasetType, sampleToCsv, validateSchema, saveTemp,
    // openInputStream, safeGet, parseDoubleSafe, findCol)
    // copy these helper methods from your previous implementation (unchanged)
    // For brevity I omitted them here — keep the same implementations as you already had.

    private Map<String, Object> computeCoreMetricsByType(String datasetTypeName, List<CSVRecord> rows, List<String> csvHeaders) {
        if ("STATION_ENERGY".equalsIgnoreCase(datasetTypeName)) {
            return computeStationEnergyMetrics(rows, csvHeaders);
        } else if ("TRANSACTION_BILLING".equalsIgnoreCase(datasetTypeName)) {
            return computeTransactionBillingMetrics(rows, csvHeaders);
        } else if ("VEHICLE_DATA_SAMPLE".equalsIgnoreCase(datasetTypeName)) {
            return computeVehicleMetrics(rows, csvHeaders);
        } else {
            Map<String, Object> out = new HashMap<>();
            out.put("rowCount", rows.size());
            Map<String, Long> nullCounts = new HashMap<>();
            for (String h : csvHeaders) nullCounts.put(h, 0L);
            for (int i = 0; i < rows.size(); i++) {
                for (String h : csvHeaders) {
                    if (safeGet(rows.get(i), h) == null || safeGet(rows.get(i), h).isBlank()) {
                        nullCounts.put(h, nullCounts.get(h) + 1);
                    }
                }
            }
            out.put("nullCounts", nullCounts);
            return out;
        }
    }

    // -------------------- HELPERS continued (original implementations) --------------------

    // STATION_ENERGY metrics
    private Map<String, Object> computeStationEnergyMetrics(List<CSVRecord> rows, List<String> csvHeaders) {
        double totalEnergy = 0.0;
        long totalSessions = rows.size();
        double sumDuration = 0.0; int cntDuration = 0;
        double sumEfficiency = 0.0; int cntEfficiency = 0;
        Double peakPower = null;

        Map<String, Double> stationEnergy = new HashMap<>();
        Map<String, double[]> daily = new TreeMap<>(); // date -> [energy, sessions]

        String colEnergy = findCol(csvHeaders, "energy_kwh", "energy", "kwh");
        String colDuration = findCol(csvHeaders, "duration_min", "duration", "session_duration");
        String colEfficiency = findCol(csvHeaders, "charging_efficiency", "efficiency");
        String colPower = findCol(csvHeaders, "power_peak_kw", "power_kw", "peak_kw");
        String colStart = findCol(csvHeaders, "start_time", "start", "created_at", "date");
        String colStation = findCol(csvHeaders, "station_id", "station", "charger_id");

        for (CSVRecord r : rows) {
            Double e = parseDoubleSafe(safeGet(r, colEnergy));
            if (e != null) totalEnergy += e;

            Double d = parseDoubleSafe(safeGet(r, colDuration));
            if (d != null) { sumDuration += d; cntDuration++; }

            Double eff = parseDoubleSafe(safeGet(r, colEfficiency));
            if (eff != null) { sumEfficiency += eff; cntEfficiency++; }

            Double p = parseDoubleSafe(safeGet(r, colPower));
            if (p != null) peakPower = (peakPower == null) ? p : Math.max(peakPower, p);

            String sid = safeGet(r, colStation);
            if (sid == null) sid = "unknown";
            if (e != null) stationEnergy.merge(sid, e, Double::sum);

            String start = safeGet(r, colStart);
            String day = (start != null && start.length() >= 10) ? start.substring(0, 10) : "unknown";
            daily.putIfAbsent(day, new double[]{0.0, 0.0});
            if (e != null) daily.get(day)[0] += e;
            daily.get(day)[1] += 1.0;
        }

        double avgDuration = cntDuration == 0 ? 0.0 : sumDuration / cntDuration;
        double avgEff = cntEfficiency == 0 ? 0.0 : sumEfficiency / cntEfficiency;
        double avgEnergyPerSession = totalSessions == 0 ? 0.0 : totalEnergy / totalSessions;

        List<java.util.Map<String, Object>> topStations = stationEnergy.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .map(e -> java.util.Map.<String, Object>of(
                        "station_id", e.getKey(),
                        "energy_kwh", e.getValue()))
                .collect(Collectors.toList());

        List<java.util.Map<String, Object>> dailyList = daily.entrySet().stream()
                .map(en -> java.util.Map.<String, Object>of(
                        "date", en.getKey(),
                        "energy", en.getValue()[0],
                        "sessions", (long) en.getValue()[1]))
                .collect(Collectors.toList());
        Map<String, Object> out = new HashMap<>();
        out.put("datasetType", "STATION_ENERGY");
        out.put("total_sessions", totalSessions);
        out.put("total_energy_kwh", totalEnergy);
        out.put("avg_energy_per_session", avgEnergyPerSession);
        out.put("avg_session_duration_min", avgDuration);
        out.put("avg_efficiency", avgEff);
        out.put("peak_power_kw", peakPower);
        out.put("top_stations", topStations);
        out.put("daily_summary", dailyList);
        return out;
    }

    // TRANSACTION_BILLING metrics
    private Map<String, Object> computeTransactionBillingMetrics(List<CSVRecord> rows, List<String> csvHeaders) {
        long totalTx = rows.size();
        double totalRevenue = 0.0;
        long success = 0, failed = 0;
        Set<String> uniqueCustomers = new HashSet<>();
        Map<String, Double> revenueByPlan = new HashMap<>();
        Map<String, double[]> daily = new TreeMap<>(); // date -> [revenue, txCount]

        String colTotalCost = findCol(csvHeaders, "total_cost", "total", "amount", "cost");
        String colStatus = findCol(csvHeaders, "payment_status", "status", "payment_status");
        String colCustomer = findCol(csvHeaders, "customer_id", "customer");
        String colPlan = findCol(csvHeaders, "pricing_plan", "plan");
        String colCreated = findCol(csvHeaders, "created_at", "created", "date");

        for (CSVRecord r : rows) {
            Double c = parseDoubleSafe(safeGet(r, colTotalCost));
            if (c != null) totalRevenue += c;

            String st = safeGet(r, colStatus);
            if (st != null && st.equalsIgnoreCase("SUCCESS")) success++;
            else if (st != null && (st.equalsIgnoreCase("FAIL") || st.equalsIgnoreCase("FAILED") || st.equalsIgnoreCase("ERROR"))) failed++;

            String cust = safeGet(r, colCustomer);
            if (cust != null) uniqueCustomers.add(cust);

            String plan = safeGet(r, colPlan);
            if (plan == null) plan = "unknown";
            revenueByPlan.merge(plan, c == null ? 0.0 : c, Double::sum);

            String created = safeGet(r, colCreated);
            String day = (created != null && created.length() >= 10) ? created.substring(0, 10) : "unknown";
            daily.putIfAbsent(day, new double[]{0.0, 0.0});
            daily.get(day)[0] += (c == null ? 0.0 : c);
            daily.get(day)[1] += 1.0;
        }

        double avgRevenuePerTx = totalTx == 0 ? 0.0 : totalRevenue / totalTx;
        int uniqueCustCount = uniqueCustomers.size();

        List<java.util.Map<String, Object>> dailyList = daily.entrySet().stream()
                .map(en -> java.util.Map.<String, Object>of(
                        "date", en.getKey(),
                        "revenue", en.getValue()[0],
                        "txCount", (long) en.getValue()[1]))
                .collect(Collectors.toList());

        Map<String, Object> out = new HashMap<>();
        out.put("datasetType", "TRANSACTION_BILLING");
        out.put("total_transactions", totalTx);
        out.put("total_revenue", totalRevenue);
        out.put("successful_transactions", success);
        out.put("failed_transactions", failed);
        out.put("avg_revenue_per_tx", avgRevenuePerTx);
        out.put("unique_customers", uniqueCustCount);
        out.put("revenue_by_plan", revenueByPlan);
        out.put("daily_revenue", dailyList);
        return out;
    }

    // VEHICLE_DATA_SAMPLE metrics
    private Map<String, Object> computeVehicleMetrics(List<CSVRecord> rows, List<String> csvHeaders) {
        Set<String> vehicleSet = new HashSet<>();
        long totalSessions = rows.size();
        double sumRequestedEnergy = 0.0; int cntRequested = 0;
        double sumSocDelta = 0.0; int cntSoc = 0;
        double sumCapacity = 0.0; int cntCapacity = 0;

        Map<String, List<Double>> energyByModel = new HashMap<>();
        String colVehicle = findCol(csvHeaders, "vehicle_id", "vehicle");
        String colModel = findCol(csvHeaders, "vehicle_model", "model");
        String colSocStart = findCol(csvHeaders, "battery_soc_start", "soc_start");
        String colSocEnd = findCol(csvHeaders, "battery_soc_end", "soc_end");
        String colRequested = findCol(csvHeaders, "requested_energy", "requested", "energy");
        String colCapacity = findCol(csvHeaders, "battery_capacity_kwh", "battery_capacity", "capacity");

        for (CSVRecord r : rows) {
            String vid = safeGet(r, colVehicle);
            if (vid != null) vehicleSet.add(vid);

            Double req = parseDoubleSafe(safeGet(r, colRequested));
            if (req != null) { sumRequestedEnergy += req; cntRequested++; }

            Double sStart = parseDoubleSafe(safeGet(r, colSocStart));
            Double sEnd = parseDoubleSafe(safeGet(r, colSocEnd));
            if (sStart != null && sEnd != null) { sumSocDelta += (sEnd - sStart); cntSoc++; }

            Double cap = parseDoubleSafe(safeGet(r, colCapacity));
            if (cap != null) { sumCapacity += cap; cntCapacity++; }

            String model = safeGet(r, colModel);
            if (model == null) model = "unknown";
            if (req != null) {
                energyByModel.computeIfAbsent(model, k -> new ArrayList<>()).add(req);
            }
        }

        double avgRequested = cntRequested == 0 ? 0.0 : sumRequestedEnergy / cntRequested;
        double avgSocDelta = cntSoc == 0 ? 0.0 : sumSocDelta / cntSoc;
        double avgCapacity = cntCapacity == 0 ? 0.0 : sumCapacity / cntCapacity;

        List<java.util.Map<String, Object>> topModels = energyByModel.entrySet().stream()
                .map(en -> java.util.Map.<String, Object>of(
                        "model", en.getKey(),
                        "avg_requested_energy", en.getValue().stream().mapToDouble(d -> d).average().orElse(0.0),
                        "count", en.getValue().size()))
                .sorted((a, b) -> Double.compare(
                        (Double) b.get("avg_requested_energy"),
                        (Double) a.get("avg_requested_energy")))
                .limit(10)
                .collect(Collectors.toList());
        Map<String, Object> out = new HashMap<>();
        out.put("datasetType", "VEHICLE_DATA_SAMPLE");
        out.put("total_vehicles", vehicleSet.size());
        out.put("total_sessions", totalSessions);
        out.put("avg_requested_energy", avgRequested);
        out.put("avg_soc_delta", avgSocDelta);
        out.put("avg_capacity_kwh", avgCapacity);
        out.put("top_models", topModels);
        return out;
    }

    // Thay thế hoàn toàn method buildPromptForDatasetType bằng method sau
    private String buildPromptForDatasetType(DatasetType dsType, List<CSVRecord> sampleRows, List<String> csvHeaders, String metricsJson) {
        String typeName = dsType.getName();
        String sampleCsv = sampleToCsv(sampleRows, csvHeaders, 50);

        if ("STATION_ENERGY".equalsIgnoreCase(typeName)) {
            // note: escape literal % -> %% to be safe with String.formatted
            return """
        Bạn là một trợ lý dữ liệu chuyên nghiệp. Tôi sẽ cung cấp CHỈ dữ liệu tổng hợp (metricsJson) đã được tính cục bộ từ dataset loại STATION_ENERGY, kèm một sample CSV để tham chiếu.
        
        Yêu cầu:
        - Dựa trên metricsJson, hãy phân tích xu hướng năng lượng và phiên sạc theo ngày.
        - Sinh 2 biểu đồ chính phù hợp với Dashboard 1 (theo spec):
          1) Line Chart: Tổng năng lượng (energy) theo ngày.
          2) Bar Chart: Số phiên (sessions) theo ngày.
        - Đưa ra insight ngắn cho mỗi biểu đồ (1-2 câu).
        - Phát hiện và liệt kê cảnh báo (alerts) nếu thỏa các điều kiện:
          * Năng lượng ngày giảm >30%% so với trung bình trước đó -> alert.
          * Spike sessions: tăng >150%% so với rolling 7-day average -> alert.
          * Nếu tìm thấy ngày thiếu dữ liệu hoặc sessions thấp bất thường -> alert.
        - Trả về **CHỈ MỘT JSON hợp lệ** (NO EXTRA TEXT) theo cấu trúc EXACT dưới đây.

        Mong muốn JSON output:
        {
          "charts": [
            {"title": "Tổng năng lượng theo ngày", "type": "line", "x": "date", "y": "energy", "insight": "..."},
            {"title": "Số phiên theo ngày", "type": "bar", "x": "date", "y": "sessions", "insight": "..."}
          ],
          "alerts": [
            {"date": "2025-01-04", "type": "energy_drop", "severity": "high", "message": "..."},
            ...
          ],
          "notes": "ngắn gọn (tối đa 2 câu) nếu cần giải thích phương pháp"
        }

        LƯU Ý:
        - Trả về JSON hợp lệ parsable (không có code fences, không mô tả thêm).
        - Sử dụng các trường có trong metricsJson: daily_summary (date, energy, sessions), top_stations, avg_efficiency, total_energy_kwh, v.v.
        - sample CSV chỉ để tham chiếu nếu cần (không bắt buộc phân tích hàng loạt).
        
        DỮ LIỆU (metricsJson): 
        %s

        SAMPLE CSV (tham chiếu):
        %s
        """.formatted(metricsJson, sampleCsv);
        } else if ("TRANSACTION_BILLING".equalsIgnoreCase(typeName)) {
            return """
        Bạn là một trợ lý dữ liệu chuyên nghiệp. Tôi cung cấp CHỈ dữ liệu tổng hợp (metricsJson) đã được tính cục bộ cho dataset TRANSACTION_BILLING, kèm sample CSV để tham chiếu.

        Yêu cầu:
        - Sinh 2 biểu đồ chính theo Dashboard 1 (spec):
          1) Line Chart: Doanh thu (revenue) theo ngày.
          2) Bar Chart: Số giao dịch (txCount) theo ngày.
        - Nêu insight chính cho mỗi biểu đồ (1-2 câu).
        - Cảnh báo nếu doanh thu ngày giảm >20%% so với trung bình 7 ngày trước.
        - Nếu có plan chiếm >60%% tổng doanh thu, báo alert "plan_dominant".
        - Trả về CHỈ MỘT JSON hợp lệ theo cấu trúc:

        {
          "charts": [
            {"title": "Doanh thu theo ngày", "type": "line", "x": "date", "y": "revenue", "insight": "..."},
            {"title": "Số giao dịch theo ngày", "type": "bar", "x": "date", "y": "txCount", "insight": "..."}
          ],
          "alerts": [
            {"date": "2025-01-03", "type": "revenue_drop", "severity": "medium", "message": "..."},
            {"type": "plan_dominant", "plan": "Premium", "share": 0.59, "message": "..."}
          ],
          "notes": "..."
        }

        DỮ LIỆU:
        %s

        SAMPLE CSV:
        %s
        """.formatted(metricsJson, sampleCsv);
        } else if ("VEHICLE_DATA_SAMPLE".equalsIgnoreCase(typeName)) {
            return """
        Bạn là một trợ lý dữ liệu chuyên nghiệp. Tôi cung cấp CHỈ dữ liệu tổng hợp (metricsJson) cho dataset VEHICLE_DATA_SAMPLE và sample CSV để tham chiếu.

        Yêu cầu:
        - Sinh 2 biểu đồ theo spec:
          1) Bar Chart: Top models theo avg_requested_energy.
          2) Scatter Plot: requested_energy vs battery_capacity_kwh.
        - Phát hiện model hoặc phiên có requested_energy vượt quá battery_capacity -> alert "over_requested".
        - Phát hiện SOC anomalies (nếu metrics cung cấp) -> alert.
        - Trả về CHỈ MỘT JSON hợp lệ:

        {
          "charts": [
            {"title": "Top model theo năng lượng yêu cầu", "type": "bar", "x": "vehicle_model", "y": "avg_requested_energy", "insight": "..."},
            {"title": "Requested energy vs battery capacity", "type": "scatter", "x": "battery_capacity_kwh", "y": "requested_energy", "insight": "..."}
          ],
          "alerts": [
            {"type": "over_requested", "model": "Model X", "count": 5, "message": "..."},
            ...
          ],
          "notes": "..."
        }

        DỮ LIỆU:
        %s

        SAMPLE CSV:
        %s
        """.formatted(metricsJson, sampleCsv);
        } else {
            return
                    """
                      Bạn là một trợ lý dữ liệu chuyên nghiệp. Tôi cung cấp CHỈ metricsJson và sample CSV cho dataset type: %s.
          
                      Yêu cầu:
                      - Dựa trên metricsJson, hãy chọn 2 biểu đồ phù hợp (time series / bar / pie / scatter / histogram).
                      - Nêu insight chính và ít nhất 1 alert có thể bật dựa trên biến động (ví dụ: drop >20%%, spike >150%%).
                      - Trả về CHỈ MỘT JSON hợp lệ với keys: charts, alerts, notes.
          
                      METRICS:
                      %s
          
                      SAMPLE CSV:
                      %s
                      """.formatted(typeName, metricsJson, sampleCsv);
        }
    }


    // Chuyển danh sách CSVRecord thành CSV string (chỉ lấy header + N dòng sample)
    private String sampleToCsv(List<CSVRecord> rows, List<String> headers, int maxRows) {
        StringBuilder sb = new StringBuilder();
        // header
        sb.append(String.join(",", headers)).append("\n");
        int limit = Math.min(maxRows, rows.size());
        for (int i = 0; i < limit; i++) {
            CSVRecord r = rows.get(i);
            List<String> values = new ArrayList<>();
            for (String h : headers) {
                String v = safeGet(r, h);
                // escape quotes and commas simply
                if (v == null) v = "";
                v = v.replace("\"", "\"\"");
                if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
                    v = "\"" + v + "\"";
                }
                values.add(v);
            }
            sb.append(String.join(",", values)).append("\n");
        }
        return sb.toString();
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
        // return absolute path
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
        return (c == null) ? null : (r.isMapped(c) ? r.get(c) : null);
    }

    private Double parseDoubleSafe(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    // đơn giản: tìm column bằng nhiều candidate keys
    private String findCol(List<String> headers, String... candidates) {
        for (String cand : candidates) {
            for (String h : headers) {
                if (h == null) continue;
                if (h.equalsIgnoreCase(cand) || h.replaceAll("\\s+", "").equalsIgnoreCase(cand.replaceAll("\\s+",""))) return h;
            }
        }
        // fallback: contains match
        for (String cand : candidates) {
            for (String h : headers) {
                if (h == null) continue;
                if (h.toLowerCase().contains(cand.toLowerCase())) return h;
            }
        }
        return null;
    }

    // ---------- AI response parsing & validation helpers ----------

    /**
     * Try to parse raw AI response string to a JsonNode.
     * Handles common wrappers like ```json fences``` and leading/trailing text.
     */
    private JsonNode parseAiRawToJson(String raw) throws IOException {
        if (raw == null) return null;
        String t = raw.strip();

        // Try full parse first
        try {
            return objectMapper.readTree(t);
        } catch (Exception ignored) {
            // fallback to fences/brace extraction
        }

        // remove leading code fence like ```json\n ... ``` or ```
        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            if (firstNewline > 0) t = t.substring(firstNewline + 1).trim();
            if (t.endsWith("```")) {
                t = t.substring(0, t.length() - 3).trim();
            }
        }

        // sometimes AI returns a JSON block inside text; try to locate the most likely JSON block
        int firstBrace = t.indexOf('{');
        int lastBrace = t.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            t = t.substring(firstBrace, lastBrace + 1);
        }

        // final trim
        t = t.trim();

        if (t.isEmpty()) return null;
        return objectMapper.readTree(t);
    }

    /**
     * Minimal schema validation for AI JSON response.
     * Ensures presence of charts (array) and alerts (array).
     */
    private boolean validateAiSchema(String datasetType, JsonNode node) {
        if (node == null || !node.isObject()) return false;
        if (!node.has("charts") || !node.get("charts").isArray()) return false;
        if (!node.has("alerts") || !node.get("alerts").isArray()) return false;
        // optional: check each chart has title and type and x/y
        for (JsonNode chart : node.get("charts")) {
            if (!chart.has("title") || !chart.has("type")) return false;
        }
        return true;
    }

    private String truncateString(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }

}
