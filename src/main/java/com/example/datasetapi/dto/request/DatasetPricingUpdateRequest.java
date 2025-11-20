package com.example.datasetapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasetPricingUpdateRequest {
    private Long pricingId;
    private double price;
    private double pricePerRequest;
}
