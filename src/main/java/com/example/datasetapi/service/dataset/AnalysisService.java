package com.example.datasetapi.service.dataset;

import com.example.datasetapi.model.dataset.DatasetAnalysis;
import com.example.datasetapi.model.dataset.DatasetInformation;
import com.example.datasetapi.model.dataset.DatasetValidationError;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface AnalysisService {
    public DatasetAnalysis saveAnalysisAndArtifacts(DatasetInformation dsInfo,
                                                    Map<String, Object> coreMetrics,
                                                    String promptText,
                                                    String aiResponseJson,
                                                    String sampleCsv,
                                                    List<DatasetValidationError> errors,
                                                    double errorRate,
                                                    long totalErrors);
}
