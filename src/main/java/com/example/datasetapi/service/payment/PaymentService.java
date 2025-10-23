package com.example.datasetapi.service.payment;

import com.example.datasetapi.enums.Datasets.BuyType;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.model.userManager.User;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService
{
//    ResponseEntity<ApiResponse> createWallet(HttpServletRequest request);


//    ResponseEntity<ApiResponse> createWallet(User user);

    boolean updateWallet(TransferType type, double amount, long user_id, BuyType buyType);

    double calRemainingAmount(double price, User consumer);
}
