package com.example.datasetapi.dto.response;

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
