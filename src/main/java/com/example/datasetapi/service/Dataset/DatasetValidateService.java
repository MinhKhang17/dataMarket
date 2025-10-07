package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetValidationError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DatasetValidateService {
    public ResponseEntity<?> uploadAndHeaderCheckCSVFile(ProviderUploadDatasetRequest providerUploadDatasetRequest, HttpServletRequest request);
    public DatasetInformation uploadAndSchemaCheckByUrl(Long datasetTypeId,
                                                        String fileUrl,
                                                        String name,
                                                        String description);
    public Map<String, Object> moderate(Long datasetId, Double thresholdPercent);

    void saveErrors(DatasetInformation datasetInformation, List<DatasetValidationError> errors);
    List<DatasetValidationError> getErrorsByDataset(DatasetInformation datasetInformation);
}
