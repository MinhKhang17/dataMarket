package com.example.datasetapi.repository;

import com.example.datasetapi.model.paySystem.Withdraw;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawRepository extends JpaRepository<Withdraw, Long> {
}
