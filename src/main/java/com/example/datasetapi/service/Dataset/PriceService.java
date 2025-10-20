package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.PricingRule;
import com.example.datasetapi.model.UserManager.User;
import org.springframework.stereotype.Service;

@Service
public interface PriceService {
    void createPricingForDataset(Dataset dataset, DatasetInformation datasetInformation);

    PricingRule findSubPricingRuleById(long pricingSubRuleId );
}
