package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.enums.TransferType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService
{
    public ResponseEntity<ApiResponse> createWallet(HttpServletRequest request);
    public boolean updateWalletAmount(TransferType type,long amount,long user_id);

}
