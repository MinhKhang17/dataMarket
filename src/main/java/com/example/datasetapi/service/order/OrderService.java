package com.example.datasetapi.service.order;

import com.example.datasetapi.dto.request.OrderRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.ConsumerOrderResponse;
import com.example.datasetapi.model.order.Order;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {
    ResponseEntity<ApiResponse> getAllOrders();

    ResponseEntity<ApiResponse> getOrdersForConsumer();

    ConsumerOrderResponse createOrder(Long userId, List<OrderRequest> orderRequest);

    ResponseEntity<ApiResponse> getOrderById(Long orderId);

    List<Order> findByPricingMethod(String pricingMethod);
}