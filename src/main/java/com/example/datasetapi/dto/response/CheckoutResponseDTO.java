package com.example.datasetapi.dto.response;

import lombok.Data;

@Data
public class CheckoutResponseDTO {
    DatasetDTO dataset;
    DatasetPricingDTO datasetPricing;
    boolean isEnough = false;
    double remaining_amount = 0.0;
}
