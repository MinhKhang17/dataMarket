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


    @PostMapping("/approve")
    public ResponseEntity<ApiResponse> processWithdrawRequest(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        ProcessWithdrawRequest withdrawRequest = objectMapper.readValue(dataJson, ProcessWithdrawRequest.class);


        return withdrawRequestService.processWithdrawApprove(withdrawRequest, file);
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse> processWithdrawRequest(@RequestBody ProcessWithdrawRequest withdrawRequest){

        return withdrawRequestService.processWithdrawReject(withdrawRequest);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> listRequest() {
        return withdrawRequestService.listRequest();
    }

    @GetMapping("/list/{status}")
    public ResponseEntity<ApiResponse> listRequestByStatus(@PathVariable("status") String status) {
        return withdrawRequestService.listRequestByStatus(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getWithdrawById(@PathVariable("id") Long id) {
        return  withdrawRequestService.getWithdrawById(id);
    }

}
