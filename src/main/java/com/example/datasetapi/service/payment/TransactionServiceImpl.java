package com.example.datasetapi.service.payment;

import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.model.paySystem.Transaction;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    private TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public boolean createTransaction(TransferType transferType, long amount, long user_id, Wallet wallet) {

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setWallet(wallet);
        transaction.setType(transferType);



        return transactionRepository.save(transaction)!=null;
    }
}
