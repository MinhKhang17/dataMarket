package com.example.datasetapi.Mapper;

import com.example.datasetapi.dto.response.DatasetParentReposonseDto;
import com.example.datasetapi.dto.response.DatasetValidationErrorDTO;
import com.example.datasetapi.dto.response.ReviewHistoryDto;
import com.example.datasetapi.dto.response.UploadHeaderResponseDto;
import com.example.datasetapi.model.Dataset.*;

public interface DatasetMapper {
    DatasetValidationErrorDTO toDatasetValidationDto(DatasetValidationError datasetValidationError);

    public DatasetParentReposonseDto toDatasetParentReposonseDto(DatasetGroup group);

    ReviewHistoryDto toReviewHistoryDto(ReviewHistory save);

    UploadHeaderResponseDto toUploadHeaderResponseDto(DatasetInformation ds);
}
