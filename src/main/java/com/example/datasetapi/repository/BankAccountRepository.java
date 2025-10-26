package com.example.datasetapi.repository;

import com.example.datasetapi.model.paySystem.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount,Long> {
}
