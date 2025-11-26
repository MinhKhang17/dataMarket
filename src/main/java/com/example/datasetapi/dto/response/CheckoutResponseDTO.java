package com.example.datasetapi.dto.response;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CheckoutResponseDTO {
    DatasetDTO dataset;
    DatasetPricingDTO datasetPricing;
    boolean isEnough = false;
    BigInteger fileSizeofDataset;
    BigInteger fileSizeOfConsumer;
}
