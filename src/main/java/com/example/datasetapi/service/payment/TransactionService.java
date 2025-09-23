package com.example.datasetapi.service.payment;

import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.model.paySystem.Transaction;
import com.example.datasetapi.model.paySystem.Wallet;
import org.springframework.stereotype.Service;

@Service
public interface TransactionService {
    public boolean createTransaction(TransferType transferType, long amount, long user_id, Wallet wallet);
}
