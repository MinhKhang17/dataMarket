package com.example.datasetapi.repository;

import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.model.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByPurchaseMethod(PricingMethod pricingMethod);

}
