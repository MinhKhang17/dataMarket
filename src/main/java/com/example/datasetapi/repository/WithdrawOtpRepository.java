package com.example.datasetapi.repository;

import com.example.datasetapi.model.paySystem.WithdrawOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WithdrawOtpRepository extends JpaRepository<WithdrawOtp, Long> {
    Optional<WithdrawOtp> findByUserIdOrderByExpireAtDesc(Long userId);
}
