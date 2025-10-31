package com.example.datasetapi.dto.request;

import lombok.Data;

@Data
public class ConsumerBuyRequestDTO {
    private long datasetId;
    
    private long datasetPricingId;

    private Boolean isHaveSub;
}
