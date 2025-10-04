package com.example.datasetapi.service.dataset;

import com.example.datasetapi.model.Dataset.DatasetInformation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface DatasetValidateService {
    public ResponseEntity<?> uploadAndHeaderCheckCSVFile(Long datasetTypeId,
                                                         MultipartFile file,
                                                         String name,
                                                         String description, HttpServletRequest request);
    public DatasetInformation uploadAndSchemaCheckByUrl(Long datasetTypeId,
                                                        String fileUrl,
                                                        String name,
                                                        String description);
    public Map<String, Object> moderate(Long datasetId, Double thresholdPercent);
}
