package com.example.datasetapi.dto.request;

import com.example.datasetapi.enums.Datasets.PricingMethod;
import lombok.Data;

@Data
public class CheckoutRequestDTO {
    private long datasetId;
    private long datasetPricingId;
    private Boolean isHaveSub;
}
