package com.example.datasetapi.service.feature;

import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.dataset.DatasetValidationError;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface FileService {
    boolean checkHeader(MultipartFile file, long datasetTypeId, Dataset ds, long user_id, DatasetSourceType datasetSourceType);
    public Map<String, Object> moderate(Dataset datasetInformation);


    public Dataset uploadAndSchemaCheckByUrl(Long datasetTypeId,
                                             String fileUrl,
                                             String name,
                                             String description);

    void saveErrors(Dataset datasetInformation, List<DatasetValidationError> errors);

    List<DatasetValidationError> getErrorsByDataset(Dataset datasetInformation);


    List<Map<String, Object>> csvToJson(File file) throws IOException;

    File convertCsvToJsonFile(Path csvPath) throws IOException;
}
