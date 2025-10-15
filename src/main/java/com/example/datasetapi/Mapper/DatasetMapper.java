package com.example.datasetapi.Mapper;

import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.model.Dataset.*;
import com.example.datasetapi.model.location.Commune;

public interface DatasetMapper {
    DatasetValidationErrorDTO toDatasetValidationDto(DatasetValidationError datasetValidationError);

    public DatasetParentReposonseDto toDatasetParentReposonseDto(DatasetGroup group);

    ReviewHistoryDto toReviewHistoryDto(ReviewHistory save);

    UploadHeaderResponseDto toUploadHeaderResponseDto(DatasetInformation ds);

    CommuneDTO toCommuneDTO(Commune commune);
}
