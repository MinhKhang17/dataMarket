package com.example.datasetapi.service.dataset;

import com.example.datasetapi.model.dataset.*;
import com.example.datasetapi.repository.AnalysisMetricsRepository;
import com.example.datasetapi.repository.AiPromptRecordRepository;
import com.example.datasetapi.repository.DatasetAnalysisRepository;
import com.example.datasetapi.repository.DatasetValidationErrorRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AnalysisServiceImpl implements AnalysisService {


   @Autowired
   private  DatasetAnalysisRepository datasetAnalysisRepository;
   @Autowired
   private  AnalysisMetricsRepository analysisMetricsRepository;
    @Autowired  AiPromptRecordRepository aiPromptRecordRepository;
    @Autowired    private  DatasetValidationErrorRepository datasetValidationErrorRepository;
    @Autowired    private  ObjectMapper objectMapper;


    /**
     * Save analysis artifacts in a single transaction:
     *  - DatasetAnalysis (create or update)
     *  - AnalysisMetrics (JSON)
     *  - AiPromptRecord (prompt + aiResponse + sampleCsv)
     *  - (Optionally) moderation errors (will replace existing)
     *
     * Note: errors argument may be null or empty.
     */
    @Override
    @Transactional
    public DatasetAnalysis saveAnalysisAndArtifacts(Dataset dsInfo,
                                                    Map<String, Object> coreMetrics,
                                                    String promptText,
                                                    String aiResponseJson,
                                                    String sampleCsv,
                                                    List<DatasetValidationError> errors,
                                                    double errorRate,
                                                    long totalErrors) {
        // 1. upsert DatasetAnalysis
        Optional<DatasetAnalysis> opt = datasetAnalysisRepository.findByDatasetInformationId(dsInfo.getId());
        DatasetAnalysis analysis = opt.orElseGet(() -> {
            DatasetAnalysis a = new DatasetAnalysis();
            a.setDatasetInformationId(dsInfo.getId());
            a.setDatasetType(dsInfo.getDatasetType() != null ? dsInfo.getDatasetType().getName() : null);
            a.setCreatedAt(Instant.now());
            return a;
        });

        analysis.setRowCount(dsInfo.getRowCount());
        analysis.setErrorRatePercent(errorRate);
        analysis.setTotalErrors(totalErrors);
        analysis.setStatus(dsInfo.getDatasetStatus() != null ? dsInfo.getDatasetStatus().name() : null);
        analysis.setUpdatedAt(Instant.now());

        analysis = datasetAnalysisRepository.save(analysis);

        // 2. save metrics as JSON (keep raw JSON for flexibility)
        AnalysisMetrics metrics = new AnalysisMetrics();
        metrics.setDatasetAnalysis(analysis);
        metrics.setType(dsInfo.getDatasetType() != null ? dsInfo.getDatasetType().getName() : null);
        try {
            metrics.setMetricsJson(objectMapper.writeValueAsString(coreMetrics == null ? Map.of() : coreMetrics));
        } catch (JsonProcessingException e) {
            // fallback to empty JSON string on serialization error
            metrics.setMetricsJson("{}");
        }
        metrics.setCreatedAt(Instant.now());
        analysisMetricsRepository.save(metrics);

        // 3. save prompt record
        AiPromptRecord promptRecord = new AiPromptRecord();
        promptRecord.setDatasetAnalysis(analysis);
        promptRecord.setPromptText(promptText);
        promptRecord.setAiResponseJson(aiResponseJson);
        promptRecord.setSampleCsv(sampleCsv);
        promptRecord.setCreatedAt(Instant.now());
        aiPromptRecordRepository.save(promptRecord);

        // 4. replace moderation errors if provided
        if (errors != null) {
            try {
                // delete previous errors linked to this DatasetInformation
                Dataset di = new Dataset();
                di.setId(dsInfo.getId());
                datasetValidationErrorRepository.deleteByDataset(di);

                // set datasetInformation on incoming errors, then save
                for (DatasetValidationError ev : errors) {
                    ev.setDataset(dsInfo);
                }
                datasetValidationErrorRepository.saveAll(errors);
            } catch (Exception e) {
                // if error saving errors, do not rollback whole transaction (application decision)
                // but since method is @Transactional it will rollback on RuntimeException.
                // we choose to surface as runtime exception so caller is aware.
                throw new RuntimeException("Failed to save dataset validation errors", e);
            }
        }

        return analysis;
    }

    /**
     * Build response payload for frontend: includes analysis, latest metrics and latest prompt record (if any).
     */
    public Map<String, Object> buildAnalysisResponse(Long analysisId) {
        DatasetAnalysis analysis = datasetAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("DatasetAnalysis not found: " + analysisId));

        // get latest metrics for this analysis (simple approach: findFirst by datasetAnalysis)
        AnalysisMetrics metrics = analysisMetricsRepository.findTopByDatasetAnalysisIdOrderByCreatedAtDesc(analysis.getId());
        AiPromptRecord prompt = aiPromptRecordRepository.findTopByDatasetAnalysisIdOrderByCreatedAtDesc(analysis.getId());

        return Map.of(
                "analysis", analysis,
                "metrics", metrics != null ? metrics.getMetricsJson() : null,
                "ai_prompt", prompt != null ? Map.of(
                        "prompt_text", prompt.getPromptText(),
                        "ai_response_json", prompt.getAiResponseJson(),
                        "sample_csv", prompt.getSampleCsv()
                ) : null
        );
    }
}