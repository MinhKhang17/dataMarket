package com.example.datasetapi.Mapper;

import com.example.datasetapi.config.ModelMapper;
import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.model.Dataset.*;
import com.example.datasetapi.model.UserManager.Provider;
import com.example.datasetapi.model.UserManager.User;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import org.apache.catalina.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class DatasetMapperImpl implements DatasetMapper {
    public DatasetMapperImpl() {
    }

    @Override
    public UploadHeaderResponseDto toUploadHeaderResponseDto(DatasetInformation ds) {
        UploadHeaderResponseDto uploadHeaderResponseDto = new UploadHeaderResponseDto();
        uploadHeaderResponseDto.setDatasetInformationId(ds.getId());
        if(ds.getValidationErrors()==null||!ds.getValidationErrors().isEmpty()){
            uploadHeaderResponseDto.setValidationErrorDtos(ds.getValidationErrors());
        }
        return uploadHeaderResponseDto;
    }

    @Override
    public ReviewHistoryDto toReviewHistoryDto(ReviewHistory save) {
        ReviewHistoryDto reviewHistoryDto = new ReviewHistoryDto();

        reviewHistoryDto.setProviderDto(toProviderDto(save.getProvider()));
        reviewHistoryDto.setDatasetDTO(toDatasetDto(save.getDataset()));
        reviewHistoryDto.setReviewId(save.getReviewId());
        if(save.getAdmin()==null){
            reviewHistoryDto.setModerator(toUserDto(save.getModerator()));
        }
        else{
            reviewHistoryDto.setAdmin(toUserDto(save.getAdmin()));
        }
        reviewHistoryDto.setReason(save.getReason());
        return reviewHistoryDto;
    }

    private UserDto toUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        return userDto;
    }

    private ProviderDto toProviderDto(Provider provider) {
        ProviderDto providerDto = new ProviderDto();
        providerDto.setId(provider.getId());
        providerDto.setName(provider.getUser().getUsername());
        providerDto.setEmail(provider.getUser().getEmail());
        return providerDto;
    }

    @Override
    public DatasetParentReposonseDto toDatasetParentReposonseDto(DatasetGroup group) {
    DatasetParentReposonseDto dto = new DatasetParentReposonseDto();
    dto.setDatasetGroupId(group.getId());
    dto.setProvinceDTO(toProvineDTO(group.getProvince()));
    dto.setLasted_upload(group.getUpdateAt());
        dto.setDatasetTypeDto(toDatasetTypeDto(group.getDatasetType()));
        if(group.getDatasetGroups()!= null){
            dto.setDatasetChildGroups(group.getDatasetGroups().stream().map(this::toDatasetChildGroupDTO).collect(Collectors.toList()));
        }
        return dto;
    }
private DatasetChildGroupDTO toDatasetChildGroupDTO(DatasetGroup datasetGroup){
        DatasetChildGroupDTO dto = new DatasetChildGroupDTO();
        dto.setCommuneDto(toComuneDTO(datasetGroup.getCommune()));
        if(datasetGroup.getDatasets()!= null){
            dto.setDatasetDtoList(datasetGroup.getDatasets().stream().map(this::toDatasetDto).collect(Collectors.toList()));
        }
        return dto;
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

    private DatasetDTO toDatasetDto(Dataset dataset) {
            DatasetDTO datasetDTO = new DatasetDTO();
            datasetDTO.setDatasetId(dataset.getId());
            datasetDTO.setVersion(dataset.getVersion());
            datasetDTO.setCreated_at(dataset.getCreated_at());
            datasetDTO.setUpdated_at(dataset.getUpdated_at());
            return datasetDTO;
    }

    private DatasetTypeDto toDatasetTypeDto(DatasetType datasetType) {
        DatasetTypeDto datasetTypeDto = new DatasetTypeDto();
        datasetTypeDto.setName(datasetType.getName());
        datasetTypeDto.setId(datasetType.getId());
        List<CategoryDto> categoryDtoList = datasetType.getCategories().
                stream().
                map(this::toCategoryDTO)
                .collect(Collectors.toList());
        datasetTypeDto.setCategoryDtoList(categoryDtoList);
        return datasetTypeDto;
    }

    private ModelMapper modelMapper;

    public CategoryDto toCategoryDTO(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        return categoryDto;
    }

    @Override
    public DatasetValidationErrorDTO toDatasetValidationDto(DatasetValidationError datasetValidationError) {
        DatasetValidationErrorDTO datasetValidationErrorDTO = new DatasetValidationErrorDTO();
        datasetValidationErrorDTO.setErrorCode(datasetValidationError.getErrorCode());
        datasetValidationErrorDTO.setMessage(datasetValidationError.getMessage());
    datasetValidationErrorDTO.setColumnName(datasetValidationError.getColumnName());
    datasetValidationErrorDTO.setRowIndex(datasetValidationError.getRowIndex());
    return datasetValidationErrorDTO;
    }
}
