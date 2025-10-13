package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.request.WithdrawRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface WithdrawRequestService {
    public ResponseEntity<ApiResponse> withdrawRequest(WithdrawRequest withdrawRequest);
}
