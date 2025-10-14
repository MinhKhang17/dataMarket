package com.example.datasetapi.dto.response;

import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.model.Dataset.DatasetValidationError;
import com.example.datasetapi.model.location.Commune;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModeratorDatasetInforResponseDto {

    private long DatasetInformationId;

    private String file_name;

    private long row_count;

    private long dataset_Type_Id;

    private long provider_id;

    private CommuneDTO communeDTO;

    private LocalDateTime createdAt;
    private LocalDateTime checkContentAt;
    private List<DatasetValidationErrorDTO> datasetValidationErrorDTOList;



}
