package com.example.datasetapi.mapper;

import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.model.dataset.DatasetInformation;
import com.example.datasetapi.model.userManager.*;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
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
        moderatorDatasetInforResponseDto.setCommuneDTO(toCommuneDTO(datasetInformation.getCommune()));
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

    private CommuneDTO toCommuneDTO(Commune commune) {
        if (commune == null) return null;
        CommuneDTO dto = new CommuneDTO();
        dto.setIdCommune(commune.getIdCommune());
        dto.setName(commune.getName());
        if (commune.getProvince() != null) {
            dto.setProvinceId(commune.getProvince().getIdProvince());
            dto.setProvinceName(commune.getProvince().getName());
        }
        return dto;
    }

    private ProvinceDTO toProvineDTO(Province province) {
            ProvinceDTO provinceDTO = new ProvinceDTO();
            provinceDTO.setProvinceName(province.getName());
            provinceDTO.setProvinceId(province.getIdProvince());
            return provinceDTO;
    }

    public ProviderRegistrationResponseDTO toProviderRegisRepsonseDTO(ProviderRegistration providerRegistration) {
        ProviderRegistrationResponseDTO dto = new ProviderRegistrationResponseDTO();
        dto.setEmail(providerRegistration.getEmail());
        dto.setId(providerRegistration.getId());
        dto.setFullName(providerRegistration.getFullName());
        dto.setOrganizationName(providerRegistration.getOrganizationName());
        dto.setTaxId(providerRegistration.getTaxId());
        dto.setLocation(Commune.getFullLocation(providerRegistration.getCommune()));
        dto.setPhoneNumber(providerRegistration.getPhoneNumber());
        dto.setRegistrationStatus(providerRegistration.getRegistrationStatus().toString());
        dto.setCreatedAt(providerRegistration.getCreatedAt());
        dto.setProviderIdentityDocumentDTOList(providerRegistration.getIdentityDocuments().stream().map(this::toProviderIdentityDocument).collect(Collectors.toList()));
        return dto;
    }

    private ProviderIdentityDocumentDTO toProviderIdentityDocument(ProviderIdentityDocument providerIdentityDocument) {
            ProviderIdentityDocumentDTO dto = new ProviderIdentityDocumentDTO();
            dto.setDocTypeName(providerIdentityDocument.getDocumentType().toString());
            dto.setImage_url(providerIdentityDocument.getImage_url());
            return dto;
    }

    public ProviderRegisReviewHistoryDTO toProviderRegisReviewHistory(ProviderRegisReviewHistory providerRegisReviewHistory) {
            ProviderRegisReviewHistoryDTO dto = new ProviderRegisReviewHistoryDTO();
            if(providerRegisReviewHistory.getProvider()!= null){
                dto.setProvider(toUserDto(providerRegisReviewHistory.getProvider().getUser()));
            }
                dto.setAdmin(toUserDto(providerRegisReviewHistory.getAdmin()));
            if(providerRegisReviewHistory.getReason()!= null){
                dto.setReason(providerRegisReviewHistory.getReason());
            }
            dto.setAccept(providerRegisReviewHistory.isAccept());
            return dto;
    }

    public UserDto toUserDto(User admin) {
        UserDto dto = new UserDto();
        dto.setId(admin.getId());
        dto.setUsername(admin.getUsername());
        dto.setActive(admin.isActive());
        if(admin.getEmail()!= null){
            dto.setEmail(admin.getEmail());
        }
        return dto;
    }


}
