package com.example.datasetapi.repository;

import com.example.datasetapi.model.paySystem.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByTxnRef(String txnRef);
    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletOrderByCreatedAtDesc(@Param("walletId") int walletId);

}
