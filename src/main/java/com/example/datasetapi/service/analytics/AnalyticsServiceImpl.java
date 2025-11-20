package com.example.datasetapi.service.analytics;

import com.example.datasetapi.dto.response.RevenueResponse;
import com.example.datasetapi.model.order.Order;
import com.example.datasetapi.model.paySystem.Transaction;
import com.example.datasetapi.service.dataset.DatasetService;
import com.example.datasetapi.service.order.OrderService;
import com.example.datasetapi.service.payment.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements  AnalyticsService {
    private final DatasetService datasetService;
    private final TransactionService transactionService;
    private final OrderService orderService;

    public Double getTotalRevenue() {
        return getSubscriptionRevenue() + getOneTimeRevenue();
    }

    public Double getOneTimeRevenue() {
        return orderService.findByPricingMethod("ONE_TIME").stream()
                .mapToDouble(Order::getTotalAmount).sum();
    }

    public Double getSubscriptionRevenue() {
        return transactionService.findByBuyType("BUY_SUB").stream()
                .mapToDouble(Transaction::getAmount).sum();
    }

    @Override
    public RevenueResponse getRevenue() {
        return new RevenueResponse(
                getTotalRevenue(),
                getOneTimeRevenue(),
                getSubscriptionRevenue()
        );
    }
}
