package com.example.datasetapi.service.payment;

import com.example.datasetapi.enums.Datasets.BuyType;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.model.dataset.ProviderRevenue;
import com.example.datasetapi.model.userManager.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PaymentService
{
    boolean updateWallet(TransferType type, double amount, long user_id, BuyType buyType);

    double calRemainingAmount(double price, User consumer);

}
