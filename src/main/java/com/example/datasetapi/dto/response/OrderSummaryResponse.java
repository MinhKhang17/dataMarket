package com.example.datasetapi.dto.response;

import com.example.datasetapi.enums.Datasets.PricingMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryResponse {
    private Long orderId;
    private LocalDateTime purchaseDate;
    private Long totalAmount;
    private PricingMethod purchaseMethod;
    private Integer datasetCount;
}
