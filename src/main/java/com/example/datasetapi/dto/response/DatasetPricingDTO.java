package com.example.datasetapi.dto.response;

import com.example.datasetapi.enums.Datasets.PricingMethod;
import lombok.Data;

@Data
public class DatasetPricingDTO {
    private long pricingId;
    private PricingMethod pricingMethod;
    private double price;
}
