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
public class AdminOrderResponse {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private List<OrderItemDetailResponse> items;
    private double totalAmount;
    private PricingMethod purchaseMethod;
    private LocalDateTime createdAt;
}
