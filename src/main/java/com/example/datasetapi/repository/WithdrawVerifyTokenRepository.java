package com.example.datasetapi.repository;

import com.example.datasetapi.model.paySystem.WithdrawVerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WithdrawVerifyTokenRepository extends JpaRepository<WithdrawVerifyToken, Long> {
    Optional<WithdrawVerifyToken> findByUserIdOrderByExpireAtDesc(Long userId);
    Optional<WithdrawVerifyToken> findByToken(String token);
}
