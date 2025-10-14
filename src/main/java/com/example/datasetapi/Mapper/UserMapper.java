package com.example.datasetapi.Mapper;

import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetValidationError;
import com.example.datasetapi.model.UserManager.User;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;

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
        moderatorDatasetInforResponseDto.setCommuneDTO(toComuneDTO(datasetInformation.getCommune()));
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

    private CommuneDTO toComuneDTO(Commune commune) {
        CommuneDTO communeDTO = new CommuneDTO();
        communeDTO.setCommuneID(commune.getIdCommune());
        communeDTO.setCommuneName(commune.getName());
        communeDTO.setProvince(toProvineDTO(commune.getProvince()));
        return communeDTO;
    }

    private ProvinceDTO toProvineDTO(Province province) {
            ProvinceDTO provinceDTO = new ProvinceDTO();
            provinceDTO.setProvinceName(province.getName());
            provinceDTO.setProvinceId(province.getIdProvince());
            return provinceDTO;
    }
}
