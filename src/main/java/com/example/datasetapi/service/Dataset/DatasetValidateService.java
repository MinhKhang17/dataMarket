package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface DatasetValidateService {
    public ResponseEntity<?> uploadAndHeaderCheckCSVFile(ProviderUploadDatasetRequest providerUploadDatasetRequest, HttpServletRequest request);
    public DatasetInformation uploadAndSchemaCheckByUrl(Long datasetTypeId,
                                                        String fileUrl,
                                                        String name,
                                                        String description);
    public Map<String, Object> moderate(Long datasetId, Double thresholdPercent);
}
