package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.service.PricingRuleDTO;
import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.dataset.DatasetInformation;
import com.example.datasetapi.model.dataset.PricingRule;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PriceService {
    void createPricingForDataset(Dataset dataset, DatasetInformation datasetInformation);

    PricingRule findSubPricingRuleById(long pricingSubRuleId );

    List<PricingRuleDTO> getAllSubPack();
}
