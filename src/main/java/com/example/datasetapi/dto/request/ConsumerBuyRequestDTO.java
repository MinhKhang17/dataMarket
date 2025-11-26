package com.example.datasetapi.dto.request;

import lombok.Data;

@Data
public class ConsumerBuyRequestDTO {
    private long datasetId;
    
    private long datasetPricingId;
    private long pricingRuleId;
    private Boolean isHaveSub;
    private String subType;
}
