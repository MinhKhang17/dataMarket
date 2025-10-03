package com.example.datasetapi.repository;

import com.example.datasetapi.model.paySystem.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByTxnRef(String txnRef);
}
