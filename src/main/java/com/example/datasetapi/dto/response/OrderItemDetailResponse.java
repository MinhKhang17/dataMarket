package com.example.datasetapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDetailResponse {
    private Long datasetId;
    private String datasetName;
    private double priceAtPurchase;
}
