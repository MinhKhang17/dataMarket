package com.example.datasetapi.dto.response;

import lombok.Data;

@Data
public class CheckoutResponseDTO {
    DatasetDTO dataset;
    //for buyOneTime
    DatasetPricingDTO datasetPricing;
    boolean isEnough = false;
    double remaining_amount = 0.0;
    //for buy with sub
    long row_amount_consumer_sub;
    long row_dataset;
}
