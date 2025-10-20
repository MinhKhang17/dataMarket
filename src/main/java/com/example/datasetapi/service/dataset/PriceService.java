package com.example.datasetapi.service.dataset;

import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.dataset.DatasetInformation;
import org.springframework.stereotype.Service;

@Service
public interface PriceService {
    void createPricingForDataset(Dataset dataset, DatasetInformation datasetInformation);

}
