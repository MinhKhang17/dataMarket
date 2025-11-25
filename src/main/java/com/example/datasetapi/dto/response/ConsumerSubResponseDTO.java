package com.example.datasetapi.dto.response;

import com.example.datasetapi.dto.service.PricingRuleDTO;
import com.example.datasetapi.enums.Datasets.SubType;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class ConsumerSubResponseDTO {
    private long consumerSubId;
    private UserDto consumer;
    private SubType subType;
    private long row_amount;
    private LocalDateTime expirationDate;
    private PricingRuleDTO pricingRuleDTO;
    private boolean isUsing = false;
    private BigInteger FileSize;
}
