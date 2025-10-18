package com.example.datasetapi.controller.ConsumerController;

import com.example.datasetapi.dto.request.CheckoutRequestDTO;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.CheckoutResponseDTO;
import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.service.Dataset.DatasetService;
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

}
