package com.example.datasetapi.dto.request;

import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.enums.Datasets.SubType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private Long datasetId;
    private String itemName;
    private double price;
    private PricingMethod pricingMethod;
    private SubType subType;
}
