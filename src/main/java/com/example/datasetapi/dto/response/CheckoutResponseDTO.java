package com.example.datasetapi.dto.response;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CheckoutResponseDTO {
    DatasetDTO dataset;
    //for buyOneTime
    DatasetPricingDTO datasetPricing;
    boolean isEnough = false;
    double remaining_amount = 0.0;
    //for buy with sub
    BigInteger row_amount_consumer_sub;
    BigInteger row_dataset;
}
