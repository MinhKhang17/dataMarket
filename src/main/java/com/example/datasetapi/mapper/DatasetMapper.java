package com.example.datasetapi.mapper;

import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.dto.service.PricingRuleDTO;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.model.dataset.*;
import com.example.datasetapi.model.userManager.ConsumerSubscription;
import com.example.datasetapi.model.location.Commune;

import java.util.List;

public interface DatasetMapper {
    DatasetValidationErrorDTO toDatasetValidationDto(DatasetValidationError datasetValidationError);

     DatasetParentReposonseDto toDatasetParentReposonseDto(DatasetGroup group);

    ReviewHistoryDto toReviewHistoryDto(ReviewHistory save);

    UploadHeaderResponseDto toUploadHeaderResponseDto(DatasetInformation ds);

    CommuneDTO toCommuneDTO(Commune commune);

    DatasetDTO toDatasetDTO(Dataset dataset);

    DatasetDTO toDatasetForCheckoutDTO(Dataset dataset);

    DatasetPricingDTO toDatasetPricingDTO(DatasetPricing datasetPricing);

    ConsumerBuyResponseDTO toConsumerBuyResponseDTO(PricingMethod pricingMethod,Object Infor);

    List<ConsumerSubResponseDTO> toConsumerSubDTO(List<ConsumerSubscription> consumerSub);
    PricingRuleDTO toPricingRuleDTO(PricingRule pricingRule);
}
