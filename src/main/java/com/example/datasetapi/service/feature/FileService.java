package com.example.datasetapi.service.feature;

import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.model.Dataset.DatasetValidationError;
import com.example.datasetapi.model.userManager.Provider;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileService {
    boolean checkHeader(MultipartFile file, long datasetTypeId, DatasetInformation ds, Provider provider);
    public Map<String, Object> moderate(Long datasetId, DatasetInformation datasetInformation, DatasetType datasetType);
    public DatasetInformation uploadAndSchemaCheckByUrl(Long datasetTypeId,
                                                        String fileUrl,
                                                        String name,
                                                        String description);

    void saveErrors(DatasetInformation datasetInformation, List<DatasetValidationError> errors);

    List<DatasetValidationError> getErrorsByDataset(DatasetInformation datasetInformation);
}
