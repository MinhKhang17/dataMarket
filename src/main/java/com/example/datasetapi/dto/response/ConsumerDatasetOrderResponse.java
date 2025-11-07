package com.example.datasetapi.dto.response;

import com.example.datasetapi.enums.Datasets.PricingMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsumerDatasetOrderResponse {
    private Long id;
    private Long amount;
    private List<String> datasetNames;
    private PricingMethod purchaseMethod;
    private LocalDateTime purchaseDate;
}
