package com.example.datasetapi.Mapper;

import com.example.datasetapi.dto.response.ModeratorDatasetInforResponseDto;
import com.example.datasetapi.model.Dataset.DatasetInformation;

public interface UserResponseDTOMapper {
   public ModeratorDatasetInforResponseDto toModeratorDatasetInforResponseDto(DatasetInformation datasetInformation);
}
