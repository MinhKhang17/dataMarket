package com.example.datasetapi.service.order;

import com.example.datasetapi.dto.request.OrderRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.ConsumerOrderResponse;
import com.example.datasetapi.model.order.Orders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface OrderService {
    ResponseEntity<ApiResponse> getAllOrders();

    ResponseEntity<ApiResponse> getOrdersForConsumer();

    ConsumerOrderResponse createOrder(Long userId, List<OrderRequest> orderRequest);

    ResponseEntity<ApiResponse> getOrderById(Long orderId);

    List<Orders> findByPricingMethod(String pricingMethod);
}