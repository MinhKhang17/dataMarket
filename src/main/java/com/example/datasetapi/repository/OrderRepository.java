package com.example.datasetapi.repository;

import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.model.order.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders,Long> {
    List<Orders> findByUserId(Long userId);
    List<Orders> findByPurchaseMethod(PricingMethod pricingMethod);

}
