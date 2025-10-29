package com.example.datasetapi.service.feature;

import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.dataset.DatasetInformation;
import com.example.datasetapi.model.dataset.DatasetValidationError;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileService {
    boolean checkHeader(MultipartFile file, long datasetTypeId, DatasetInformation ds, User provider);
    public Map<String, Object> moderate(DatasetInformation datasetInformation);
    public DatasetInformation uploadAndSchemaCheckByUrl(Long datasetTypeId,
                                                        String fileUrl,
                                                        String name,
                                                        String description);

    void saveErrors(DatasetInformation datasetInformation, List<DatasetValidationError> errors);

    List<DatasetValidationError> getErrorsByDataset(DatasetInformation datasetInformation);

}
