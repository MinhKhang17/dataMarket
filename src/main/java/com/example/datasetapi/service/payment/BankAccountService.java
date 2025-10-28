package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.request.BankRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.paySystem.BankAccount;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface BankAccountService {
    BankAccount getBankAccount(Long id);
    ResponseEntity<ApiResponse> addBankAccount(BankRequest bankRequest);
    List<BankAccount> getBankByUserId(Long userId);
    BankAccount addBankAccount(BankRequest request, Long userId);
    ResponseEntity<ApiResponse> listbank();
    ResponseEntity<ApiResponse> getBankById(Long id);
}
