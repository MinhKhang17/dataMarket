package com.example.datasetapi.dto.response;

import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.model.Dataset.DatasetValidationError;
import lombok.Data;

import java.util.List;

@Data
public class ModeratorDatasetInforResponseDto {

    private DatasetInformation DatasetInformationId;

    private String file_name;

    private long row_count;

    private DatasetType datasetType;

    private long provider_id;

    private List<DatasetValidationErrorDTO> datasetValidationErrorDTOList;


}
