package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.request.ProcessWithdrawRequest;
import com.example.datasetapi.dto.request.WithdrawRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface WithdrawService {
    ResponseEntity<ApiResponse> withdrawRequest(WithdrawRequest withdrawRequest);
    ResponseEntity<ApiResponse> processWithdrawRequest(ProcessWithdrawRequest withdrawRequest);
}
