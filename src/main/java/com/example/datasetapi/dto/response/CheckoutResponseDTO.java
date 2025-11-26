package com.example.datasetapi.dto.response;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CheckoutResponseDTO {
    DatasetDTO dataset;
    //for buyOneTime
    DatasetPricingDTO datasetPricing;
    boolean isEnough = false;
    //for buy with sub
    BigInteger fileSizeofDataset;
    BigInteger fileSizeOfConsumer;
}
