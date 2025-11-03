package com.example.datasetapi.service.order;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.ConsumerDatasetOrderResponse;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {
    ResponseEntity<ApiResponse> getAllOrders();

    ResponseEntity<ApiResponse> getOrdersForConsumer();

    ConsumerDatasetOrderResponse createOrder(Long userId, List<Long> datasetId, Long amount, PricingMethod purchaseMethod);

    ResponseEntity<ApiResponse> getOrderById(Long orderId);
}