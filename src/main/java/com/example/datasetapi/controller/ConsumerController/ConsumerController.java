package com.example.datasetapi.controller.ConsumerController;

import com.example.datasetapi.dto.request.CheckoutRequestDTO;
import com.example.datasetapi.dto.request.ConsumerBuyRequestDTO;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.CheckoutResponseDTO;
import com.example.datasetapi.dto.response.ConsumerBuyResponseDTO;
import com.example.datasetapi.enums.Datasets.SubType;
import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.UserManager.ConsumerSubscription;
import com.example.datasetapi.service.Dataset.DatasetService;
import com.example.datasetapi.service.user.ConsumerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/consumer/")
public class ConsumerController {
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private ConsumerService consumerService;
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

    @PostMapping("dataset/SubRegister")
    public ResponseEntity<ApiResponse> buySub(@ModelAttribute long datasetSubPlanId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.subRegister(datasetSubPlanId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"sub buy loading success",consumerBuyResponseDTO));
    }

    @GetMapping("sub/mySub")
    public ResponseEntity<ApiResponse> subMySub(@ModelAttribute long datasetSubPlanId, HttpServletRequest request){
        List<ConsumerSubscription> consumerSubscriptions = consumerService.getConsumerSubscriptions(request);
    return ResponseEntity.ok().body(new ApiResponse(true,"sub loading success",consumerSubscriptions));
    }

    @PostMapping("sub/select")
    public ResponseEntity<ApiResponse> selectSub(@ModelAttribute long consumerSubId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.selectSubPack(consumerSubId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"sub selecting success",consumerBuyResponseDTO));
    }
}
