package com.example.datasetapi.controller.payment;

import com.example.datasetapi.dto.request.WithdrawRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.payment.WithdrawRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/withdraw")
@RequiredArgsConstructor
public class WithdrawController {
    private final WithdrawRequestService withdrawRequestService;
    @PostMapping("/request")
    public ResponseEntity<ApiResponse> withdrawRequest(@RequestBody WithdrawRequest withdrawRequest) {
        return withdrawRequestService.withdrawRequest(withdrawRequest);
    }
}
