package com.example.datasetapi.controller.payment;

import com.example.datasetapi.dto.request.ProcessWithdrawRequest;
import com.example.datasetapi.dto.request.WithdrawRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.feature.ImageService;
import com.example.datasetapi.service.payment.WithdrawService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/withdraw")
@RequiredArgsConstructor
public class WithdrawController {
    private final ImageService imageService;
    private final WithdrawService withdrawRequestService;
    @PostMapping("/request")
    public ResponseEntity<ApiResponse> withdrawRequest(@RequestBody WithdrawRequest withdrawRequest) {
        return withdrawRequestService.withdrawRequest(withdrawRequest);
    }
    @PostMapping("/process")
    public ResponseEntity<ApiResponse> processWithdrawRequest(
            @RequestPart("data") String dataJson,
            @RequestPart("file") MultipartFile file) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        ProcessWithdrawRequest withdrawRequest = objectMapper.readValue(dataJson, ProcessWithdrawRequest.class);

        String imageUrl = imageService.uploadImage(file);

        withdrawRequest.setProofImageUrl(imageUrl);
        return withdrawRequestService.processWithdrawRequest(withdrawRequest);
    }

}
