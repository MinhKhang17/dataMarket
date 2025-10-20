package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.enums.Datasets.BuyType;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.model.userManager.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService
{
    public ResponseEntity<ApiResponse> createWallet(HttpServletRequest request);
     boolean updateWallet(TransferType type, double amount, long user_id, BuyType buyType);

    double calRemainingAmount(double price, User consumer);
}
