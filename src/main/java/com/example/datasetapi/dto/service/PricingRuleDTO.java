package com.example.datasetapi.dto.service;

import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.enums.Datasets.SubType;
import lombok.Data;

import java.math.BigInteger;

@Data
public class PricingRuleDTO {
    private long pricing_rule_id;
    private PricingMethod pricingMethod;
    private String planName;
    private long row_limit_of_this_pack;
    private double base_price;
    private String pricing_Rule_name;
    private BigInteger fileSize;
    private long dayLimit;
}
