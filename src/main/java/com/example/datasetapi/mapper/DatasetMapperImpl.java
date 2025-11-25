package com.example.datasetapi.mapper;

import com.example.datasetapi.config.ModelMapper;
import com.example.datasetapi.dto.ProviderRevenueDTO;
import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.dto.service.*;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.model.dataset.*;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.model.userManager.ConsumerSubscription;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import com.example.datasetapi.repository.DatasetPlanRepo;
import com.example.datasetapi.repository.DatasetPreviewRepository;
import com.example.datasetapi.service.dataset.DatasetServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;



@Component
public class DatasetMapperImpl implements DatasetMapper {
    @Autowired
    private DatasetPlanRepo datasetPlanRepo;
    @Autowired private DatasetPreviewRepository datasetPreviewRepository;
    public DatasetMapperImpl() {
    }
    private static final Logger logger = LoggerFactory.getLogger(DatasetServiceImpl.class);
    @Autowired
    private ObjectMapper objectMapper;
    @Override
    public UploadHeaderResponseDto toUploadHeaderResponseDto(Dataset ds) {
        UploadHeaderResponseDto uploadHeaderResponseDto = new UploadHeaderResponseDto();
        uploadHeaderResponseDto.setDatasetInformationId(ds.getId());
        if(ds.getValidationErrors()==null||!ds.getValidationErrors().isEmpty()){
            uploadHeaderResponseDto.setValidationErrorDtos(ds.getValidationErrors());
        }
        return uploadHeaderResponseDto;
    }

    @Override
    public DatasetDTO toDatasetDTO(Dataset dataset) {
        DatasetDTO datasetDTO = new DatasetDTO();
        datasetDTO.setDatasetId(dataset.getId());
//        datasetDTO.setVersion(dataset.getVersion());
            datasetDTO.setDatasetPLanWithPricingDTO(datasetPlanRepo.findALlByDatasetId(dataset.getId())
                    .stream()
                    .map(this::toDatasetPlanWithPricingDTO)
                    .collect(Collectors.toList()));

        if(dataset.getTimeGroup()!=null){
            datasetDTO.setDatasetTime(TimeGroup.toDate(dataset.getTimeGroup()));
        }
//        datasetDTO.setProvider(toProviderDto(dataset.getProvider()));
        datasetDTO.setTitle(dataset.getTitle());
        datasetDTO.setDescription(dataset.getDescription());
        datasetDTO.setProvince(dataset.getDatasetChildGroup().getParent().getProvince().getName());
        datasetDTO.setCommune(dataset.getDatasetChildGroup().getCommune().getName());
        datasetDTO.setDatasetTime(TimeGroup.toDate(dataset.getTimeGroup()));
        datasetDTO.setCategory(dataset.getDatasetChildGroup().getParent().getDatasetType().getCategories());
datasetDTO.setRow_amount(dataset.getRowCount());
        DatasetPreview preview = datasetPreviewRepository.findByDataset(dataset).orElse(null);
            if (preview==null){
                System.out.println("null roi");}
        if (preview != null) {
            try {
                datasetDTO.setPreviewHeaders(objectMapper.readValue(preview.getHeadersJson(), List.class));
                datasetDTO.setPreviewRows(objectMapper.readValue(preview.getRowsJson(), List.class));
            } catch (Exception e) {
            }
        }
        return datasetDTO;
        }

    @Override
    public DatasetDTO toDatasetForCheckoutDTO(Dataset dataset) {
            DatasetDTO datasetDTO = new DatasetDTO();
            datasetDTO.setDatasetId(dataset.getId());
            datasetDTO.setDescription(dataset.getDescription());
            datasetDTO.setDatasetTime(TimeGroup.toDate(dataset.getTimeGroup()));
//            datasetDTO.setProvider(toProviderDto(dataset.getProvider()));
            datasetDTO.setTitle(dataset.getTitle());
            return datasetDTO;
    }

    private DatasetPLanWithPricingDTO toDatasetPlanWithPricingDTO(DatasetPlan datasetPlan) {
    DatasetPLanWithPricingDTO datasetPLanWithPricingDTO = new DatasetPLanWithPricingDTO();
    datasetPLanWithPricingDTO.setPricingMethod(datasetPlan.getPricingMethod());
    datasetPLanWithPricingDTO.setDatasetPricingDTOList(datasetPlan.getDatasetPricingList()
            .stream()
            .map(this::toDatasetPricingDTO)
            .collect(Collectors.toList()));
    return datasetPLanWithPricingDTO;
    }

    public DatasetPricingDTO toDatasetPricingDTO(DatasetPricing datasetPricing) {
        DatasetPricingDTO datasetPricingDTO = new DatasetPricingDTO();
        if(datasetPricing.getPricingRule().getMethod()== PricingMethod.API){
            datasetPricingDTO.setPricingId(datasetPricing.getId());
            datasetPricingDTO.setPrice(datasetPricing.getPrice());
            datasetPricingDTO.setPricingMethod(datasetPricing.getPricingRule().getMethod());
            datasetPricingDTO.setRequestLimit(datasetPricing.getPricingRule().getRequestLimit());
            return datasetPricingDTO;
        }
        datasetPricingDTO.setPrice(datasetPricing.getPrice());
        datasetPricingDTO.setPricingMethod(datasetPricing.getPricingRule().getMethod());
        datasetPricingDTO.setPricingId(datasetPricing.getId());
        if(datasetPricing.getPricingMethod() == PricingMethod.SUBSCRIPTION){
            datasetPricingDTO.setSubType(datasetPricing.getPricingRule().getSubType());
        }

        return datasetPricingDTO;
    }

    @Override
    public ConsumerBuyResponseDTO toConsumerBuyResponseDTO(PricingMethod pricingMethod,Object infor) {
        switch (pricingMethod){
            case ONE_TIME -> {
                    ConsumerBuyResponseDTO consumerBuyResponseDTO = new ConsumerBuyResponseDTO();
                    consumerBuyResponseDTO.setBuyOnTimeInfoDTO(toBuyOneTimeInfoDTO((UUID)infor));
                    return consumerBuyResponseDTO;
            }
            case SUBSCRIPTION ->  {
                    ConsumerBuyResponseDTO consumerBuyResponseDTO = new ConsumerBuyResponseDTO();
                    consumerBuyResponseDTO.setBuySubInfoDTO(toBuySubInfoDTO((ConsumerSubscription)infor));
                return consumerBuyResponseDTO;
            }
            case BUY_WITH_TIME_GROUP -> {
                    ConsumerBuyResponseDTO consumerBuyResponseDTO = new ConsumerBuyResponseDTO();
                    consumerBuyResponseDTO.setBuyWithGroupDTO(toBuyWithGroupDTO((DownloadToken)infor));
                    return consumerBuyResponseDTO;
            }
            case API -> {
                ConsumerBuyResponseDTO consumerBuyResponseDTO = new ConsumerBuyResponseDTO();
                consumerBuyResponseDTO.setBuyApiInforDTO(toBuyApiInfoDTO((String)infor));
                return consumerBuyResponseDTO;
            }
        }
        return null;
    }

    private BuyApiInforDTO toBuyApiInfoDTO(String infor) {
        BuyApiInforDTO buyApiInforDTO = new BuyApiInforDTO();
        buyApiInforDTO.setDownLoadToken(infor);
        return  buyApiInforDTO;
    }

    public BuyWithGroupDTO toBuyWithGroupDTO(DownloadToken infor) {
            BuyWithGroupDTO buyWithGroupDTO = new BuyWithGroupDTO();
            buyWithGroupDTO.setToken(infor.getId().toString());
            return buyWithGroupDTO;
    }

    @Override
    public List<ConsumerSubResponseDTO> toConsumerSubDTO(List<ConsumerSubscription> consumerSub) {
        return consumerSub
                .stream()
                .map(this::toConsumerSubReponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConsumerSubResponseDTO toConsumerSubReponseDTO(ConsumerSubscription consumerSubscription) {
            ConsumerSubResponseDTO consumerSubResponseDTO = new ConsumerSubResponseDTO();
            consumerSubResponseDTO.setConsumerSubId(consumerSubscription.getId());
            consumerSubResponseDTO.setConsumer(toUserDto(consumerSubscription.getConsumer()));
            consumerSubResponseDTO.setSubType(consumerSubscription.getSubType());
            consumerSubResponseDTO.setUsing(consumerSubscription.isUsing());
            consumerSubResponseDTO.setExpirationDate(consumerSubscription.getExpiresAt());
            consumerSubResponseDTO.setFileSize(consumerSubscription.getFileSize());
            consumerSubResponseDTO.setPricingRuleDTO(toPricingRuleDTO(consumerSubscription.getPricingRule()));
        return consumerSubResponseDTO;
    }

    public PricingRuleDTO toPricingRuleDTO(PricingRule pricingRule) {
            PricingRuleDTO pricingRuleDTO = new PricingRuleDTO();
            pricingRuleDTO.setPricing_rule_id(pricingRule.getId());
            pricingRuleDTO.setPricingMethod(pricingRule.getMethod());
            pricingRuleDTO.setPlanName(pricingRule.getPlanName());
            pricingRuleDTO.setBase_price(pricingRule.getBasePricePoint());
            pricingRuleDTO.setFileSize(pricingRule.getFileSize());
            if(pricingRule.getRowLimit()!=null){
                pricingRuleDTO.setRow_limit_of_this_pack(pricingRule.getRowLimit());
            }
            if(pricingRule.getDatasetType()!=null){
                pricingRuleDTO.setPricing_Rule_name(pricingRule.getDatasetType().getName());
            }
            return pricingRuleDTO;
    }

    @Override
    public TimeGroupDTO toTimeGroupDTO(TimeGroup timeGroup) {
        TimeGroupDTO timeGroupDTO = new TimeGroupDTO();
       timeGroupDTO.setTimeGroupId(timeGroup.getId());
        timeGroupDTO.setMonth(timeGroup.getMonth());
        timeGroupDTO.setYear(timeGroup.getYear());
        return timeGroupDTO;
    }

    @Override
    public DatasetGroupInfor toDatasetGroupInfor(DatasetGroup datasetGroup) {
        DatasetGroupInfor datasetGroupInfor = new DatasetGroupInfor();
        datasetGroupInfor.setDataset_Type_id(datasetGroup.getDatasetType().getId());
        datasetGroupInfor.setCommune_name(datasetGroup.getCommune().getName());
        datasetGroupInfor.setCommune_id(datasetGroup.getCommune().getIdCommune());
        datasetGroupInfor.setProvine_id(datasetGroup.getCommune().getProvince().getIdProvince());
        datasetGroupInfor.setProvine_name(datasetGroup.getCommune().getProvince().getName());
        datasetGroupInfor.setSetDataset_Type_Name(datasetGroup.getDatasetType().getName());
        return datasetGroupInfor;
    }

    @Override
    public ProviderRevenueDTO toProviderRevenueDTO(ProviderRevenue providerRevenue) {

        return new ProviderRevenueDTO(toDatasetDTO(providerRevenue.getDataset()),providerRevenue.getRevenue_amount(),providerRevenue.getId(),providerRevenue.getCreatedAt());
    }

    @Override
    public ApiTokenResponse toApiTokenResponse(ApiAccessToken apiAccessToken) {
        ApiTokenResponse apiTokenResponse = new ApiTokenResponse();
        apiTokenResponse.setToken_id(apiAccessToken.getId());
        apiTokenResponse.setDataset(toDatasetDTO(apiAccessToken.getDataset()));
        apiTokenResponse.setUserAmount(apiAccessToken.getUseAmount());
        apiTokenResponse.setUserCount(apiAccessToken.getUsesCount());
        apiTokenResponse.setExpiresAt(apiAccessToken.getExpiresAt());
        return apiTokenResponse;
    }

    private BuySubInfoDTO toBuySubInfoDTO(ConsumerSubscription infor) {
        BuySubInfoDTO buySubInfoDTO = new BuySubInfoDTO();
        buySubInfoDTO.setSubType(infor.getSubType());
    buySubInfoDTO.setFileSize(infor.getFileSize());
    buySubInfoDTO.setExpiredDay(infor.getExpiresAt());

    return buySubInfoDTO;
    }


    private BuyOnTimeInfoDTO toBuyOneTimeInfoDTO(UUID infor) {
        BuyOnTimeInfoDTO buyOnTimeInfoDTO = new BuyOnTimeInfoDTO();
        buyOnTimeInfoDTO.setDownloadToken(infor.toString());
        return buyOnTimeInfoDTO;
    }


    @Override
    public ReviewHistoryDto toReviewHistoryDto(ReviewHistory save) {
        ReviewHistoryDto reviewHistoryDto = new ReviewHistoryDto();

        // ✅ Dùng mapper safe (không serialize toàn bộ entity)
        reviewHistoryDto.setProviderDto(toProviderDtoSafe(save.getProvider()));
        reviewHistoryDto.setDatasetDTO(toDatasetDtoSafe(save.getDataset()));
        reviewHistoryDto.setReviewId(save.getReviewId());

        if(save.getAdmin() == null){
            reviewHistoryDto.setModerator(toUserDtoSafe(save.getModerator()));
        } else {
            reviewHistoryDto.setAdmin(toUserDtoSafe(save.getAdmin()));
        }

        reviewHistoryDto.setReason(save.getReason());
        return reviewHistoryDto;
    }
    private ProviderDto toProviderDtoSafe(Provider provider) {
        if (provider == null) return null;

        ProviderDto dto = new ProviderDto();
        dto.setId(provider.getId());
        dto.setName(provider.getProviderRegistration().getFullName());


        // ✅ CHỈ convert User cơ bản, KHÔNG convert toàn bộ Provider trong User
        if (provider.getUser() != null) {
            UserDto userDto = new UserDto();
            userDto.setId(provider.getUser().getId());
            userDto.setUsername(provider.getUser().getUsername());
            userDto.setEmail(provider.getUser().getEmail());
             // KHÔNG set provider trong userDto để tránh vòng lặp

        }

        return dto;
    }

    /**
     * ✅ Convert Dataset sang DTO mà KHÔNG gây StackOverflow
     */
    private DatasetDTO toDatasetDtoSafe(Dataset dataset) {
        if (dataset == null) return null;

        DatasetDTO dto = new DatasetDTO();
        dto.setTitle(dataset.getTitle());
        dto.setDescription(dataset.getDescription());


        // ✅ CHỈ lấy tên, KHÔNG convert toàn bộ entity để tránh vòng lặp
        if (dataset.getDatasetChildGroup() != null) {
            if (dataset.getDatasetChildGroup().getDatasetType() != null) {
                dto.setDatasetType(dataset.getDatasetChildGroup().getDatasetSourceType());
            }

            if (dataset.getDatasetChildGroup().getCommune() != null) {
                dto.setCommune(dataset.getDatasetChildGroup().getCommune().getName());

                if (dataset.getDatasetChildGroup().getCommune().getProvince() != null) {
                    dto.setProvince(dataset.getDatasetChildGroup().getCommune().getProvince().getName());

                    if (dataset.getDatasetChildGroup().getCommune().getProvince() != null) {
                        dto.setProvince(dataset.getDatasetChildGroup().getCommune().getProvince().getName());
                    }
                }
            }
        }

        // KHÔNG set DatasetGroup, TimeGroup, Provider để tránh vòng lặp

        return dto;
    }

    /**
     * ✅ Convert User sang DTO mà KHÔNG gây StackOverflow
     */
    private UserDto toUserDtoSafe(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());


        // KHÔNG set provider/admin/moderator để tránh vòng lặp

        return dto;
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
        providerDto.setName(provider.getProviderRegistration().getOrganizationName());
        return providerDto;
    }

    @Override
    public DatasetParentReposonseDto toDatasetParentReposonseDto(DatasetGroup group) {
    DatasetParentReposonseDto dto = new DatasetParentReposonseDto();
    dto.setDatasetGroupId(group.getId());
    dto.setDatasetSourceType(group.getDatasetSourceType());
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
        dto.setDatasetChildGroupId(datasetGroup.getId());
        dto.setCommuneDto(toCommuneDTO(datasetGroup.getCommune()));
        if(datasetGroup.getDatasets()!= null){
            dto.setDatasetDtoList(datasetGroup.getDatasets().stream()
                            .filter(dataset -> dataset.getDatasetStatus()==DatasetStatus.APPROVE)
                    .map(this::toDatasetDto)
                    .collect(Collectors.toList()));
        }
        dto.setTimeGroupDtoList(datasetGroup.getTimeGroups().stream().map(this::toTimeGroupDTO).collect(Collectors.toList()));
        return dto;
}
    public CommuneDTO toCommuneDTO(Commune commune) {
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

    private DatasetDTO toDatasetDto(Dataset dataset) {
            DatasetDTO datasetDTO = new DatasetDTO();
            datasetDTO.setDatasetId(dataset.getId());
            datasetDTO.setTitle(dataset.getTitle());
            datasetDTO.setDescription(dataset.getDescription());
            datasetDTO.setDatasetTime(TimeGroup.toDate(dataset.getTimeGroup()));
//            datasetDTO.setProvider(toProviderDto(dataset.getProvider()));
            datasetDTO.setDatasetPLanWithPricingDTO(dataset.getDatasetPlans().stream().map(this::toDatasetPlanWithPricingDTO).collect(Collectors.toList()));
            datasetDTO.setRow_amount(dataset.getRowCount());
        DatasetPreview preview = datasetPreviewRepository.findByDataset(dataset).orElse(null);
        if (preview != null) {
            try {
                datasetDTO.setPreviewHeaders(objectMapper.readValue(preview.getHeadersJson(), List.class));
                datasetDTO.setPreviewRows(objectMapper.readValue(preview.getRowsJson(), List.class));
            } catch (Exception e) {
            }
        }
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
