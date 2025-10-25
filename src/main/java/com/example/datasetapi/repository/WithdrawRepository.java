package com.example.datasetapi.repository;

import com.example.datasetapi.model.paySystem.Withdraw;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawRepository extends JpaRepository<Withdraw, Long> {
    List<Withdraw> findWithdrawByStatus(Withdraw.Status status);
}
