package com.example.datasetapi.mapper;

import com.example.datasetapi.dto.response.ModeratorDatasetInforResponseDto;
import com.example.datasetapi.model.dataset.DatasetInformation;

public interface UserResponseDTOMapper {
   public ModeratorDatasetInforResponseDto toModeratorDatasetInforResponseDto(DatasetInformation datasetInformation);
}
