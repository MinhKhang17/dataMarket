package com.example.datasetapi.Mapper;

import com.example.datasetapi.dto.response.DatasetValidationErrorDTO;
import com.example.datasetapi.dto.response.ModeratorDatasetInforResponseDto;
import com.example.datasetapi.dto.response.UserInformationResponseForAuthMe;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetValidationError;
import com.example.datasetapi.model.UserManager.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper implements UserResponseDTOMapper {
    private DatasetMapper datasetMapper = new  DatasetMapperImpl();
    public static UserInformationResponseForAuthMe toUserInformationResponseForAuthMeDTO(User user) {

        UserInformationResponseForAuthMe userInformationResponse = new UserInformationResponseForAuthMe();
        userInformationResponse.setUser_name(user.getUsername());
        userInformationResponse.setEmail(user.getEmail());
        userInformationResponse.setUser_id(user.getId());
        userInformationResponse.setRole(user.getRole().getName());
        return userInformationResponse;
    }

    @Override
    public ModeratorDatasetInforResponseDto toModeratorDatasetInforResponseDto(DatasetInformation datasetInformation) {

        ModeratorDatasetInforResponseDto moderatorDatasetInforResponseDto = new ModeratorDatasetInforResponseDto();
        moderatorDatasetInforResponseDto.setDataset_Type_Id(datasetInformation.getDatasetType().getId());
        moderatorDatasetInforResponseDto.setDatasetInformationId(datasetInformation.getId());
        moderatorDatasetInforResponseDto.setFile_name(datasetInformation.getName());
        moderatorDatasetInforResponseDto.setRow_count(datasetInformation.getRowCount());
        moderatorDatasetInforResponseDto.setProvider_id(datasetInformation.getProvider().getId());
        moderatorDatasetInforResponseDto.setFullAddress(datasetInformation.getLocation().getFullLocation());
        moderatorDatasetInforResponseDto.setCreatedAt(datasetInformation.getCreateAt());
        moderatorDatasetInforResponseDto.setCheckContentAt(datasetInformation.getUpdateAt());
        List<DatasetValidationErrorDTO> datasetValidationErrorDTOList =
                datasetInformation.getDatasetValidationErrorList()
                        .stream()
                        .map(datasetMapper::toDatasetValidationDto)
                        .collect(Collectors.toList());

        moderatorDatasetInforResponseDto.setDatasetValidationErrorDTOList(datasetValidationErrorDTOList);

        return moderatorDatasetInforResponseDto;

    }
}
