package com.example.datasetapi.mapper;

import com.example.datasetapi.dto.response.DatasetReposonseDto;
import com.example.datasetapi.dto.response.DatasetValidationErrorDTO;
import com.example.datasetapi.dto.response.ReviewHistoryDto;
import com.example.datasetapi.dto.response.UploadHeaderResponseDto;
import com.example.datasetapi.model.dataset.*;

public interface DatasetMapper {
    DatasetValidationErrorDTO toDatasetValidationDto(DatasetValidationError datasetValidationError);
    DatasetReposonseDto  toDatasetReposonseDto(DatasetGroup group);

    ReviewHistoryDto toReviewHistoryDto(ReviewHistory save);

    UploadHeaderResponseDto toUploadHeaderResponseDto(DatasetInformation ds);
}
