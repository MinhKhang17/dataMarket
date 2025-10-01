package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.model.Dataset.DatasetInfor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface DatasetValidateService {
    public ResponseEntity<?> uploadAndHeaderCheck(Long datasetTypeId,
                                                  MultipartFile file,
                                                  String name,
                                                  String description);
    public DatasetInfor uploadAndSchemaCheckByUrl(Long datasetTypeId,
                                                  String fileUrl,
                                                  String name,
                                                  String description);
    public Map<String, Object> moderate(Long datasetId, Double thresholdPercent);
}
