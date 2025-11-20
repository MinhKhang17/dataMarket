package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.enums.Datasets.BuyType;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.model.paySystem.Transaction;
import com.example.datasetapi.model.paySystem.Wallet;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TransactionService {
     Transaction createTransaction(TransferType transferType, double amount, long user_id, Wallet wallet, BuyType buyType);

     ResponseEntity<ApiResponse> getTransactions();
     List<Transaction> findByBuyType(String buyType);
}
