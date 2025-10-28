package com.example.datasetapi.repository;

import com.example.datasetapi.model.paySystem.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount,Long> {
    List<BankAccount> findBankAccountByUserId(Long userId);
}
