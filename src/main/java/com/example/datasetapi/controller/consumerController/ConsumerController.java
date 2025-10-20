package com.example.datasetapi.controller.consumerController;

import com.example.datasetapi.dto.request.CheckoutRequestDTO;
import com.example.datasetapi.dto.request.ConsumerBuyRequestDTO;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.CheckoutResponseDTO;
import com.example.datasetapi.dto.response.ConsumerBuyResponseDTO;
import com.example.datasetapi.service.dataset.DatasetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/consumer/")
public class ConsumerController {
    @Autowired
    private DatasetService datasetService;
    @PostMapping("dataset/checkout")
    public ResponseEntity<ApiResponse> checkout(@ModelAttribute CheckoutRequestDTO checkoutRequestDTO, HttpServletRequest request){
        CheckoutResponseDTO checkoutResponseDTO = datasetService.checkoutDatasetPayment(checkoutRequestDTO,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"checkout loading success",checkoutResponseDTO));
    }

    @PostMapping("dataset/buy")
    public ResponseEntity<ApiResponse> buyDataset(@ModelAttribute ConsumerBuyRequestDTO buyRequestDTO, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyDatasetRequest(buyRequestDTO,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"buy loading success",consumerBuyResponseDTO));
    }

}
