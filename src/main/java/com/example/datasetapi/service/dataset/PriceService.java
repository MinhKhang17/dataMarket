package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.dto.service.PricingRuleDTO;
import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.dataset.PricingRule;
import com.example.datasetapi.model.userManager.Provider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PriceService {
    void createPricingForDataset(Dataset dataset, ProviderUploadDatasetRequest request);

    PricingRule findSubPricingRuleById(long pricingSubRuleId );

    List<PricingRuleDTO> getAllSubPack();

    List<PricingRuleDTO> getAllAPIPricingRule();

    PricingRule findApiPricingRuleById(long apiPackId);

    void createRevenueForProvider(Provider provider, DatasetPack datasetPack, Dataset dataset);
}
