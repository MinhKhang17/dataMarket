package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.dto.service.PricingRuleDTO;
import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.PricingRule;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PriceService {
    void createPricingForDataset(Dataset dataset, DatasetInformation datasetInformation);

    PricingRule findSubPricingRuleById(long pricingSubRuleId );

    List<PricingRuleDTO> getAllSubPack();

    List<PricingRuleDTO> getAllAPIPricingRule();

    PricingRule findApiPricingRuleById(long apiPackId);
}
