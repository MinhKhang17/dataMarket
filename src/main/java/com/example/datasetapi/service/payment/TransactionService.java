package com.example.datasetapi.service.payment;

import com.example.datasetapi.enums.Datasets.BuyType;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.model.paySystem.Transaction;
import com.example.datasetapi.model.paySystem.Wallet;
import org.springframework.stereotype.Service;

@Service
public interface TransactionService {
     Transaction createTransaction(TransferType transferType, double amount, long user_id, Wallet wallet, BuyType buyType);
}
