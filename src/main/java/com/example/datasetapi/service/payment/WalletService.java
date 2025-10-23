package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.model.userManager.User;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface WalletService {
    ResponseEntity<ApiResponse> createWallet(User user);
    Optional<Wallet> findWalletByUserId(Long id);
}
