package com.example.datasetapi.controller.ConsumerController;

import com.example.datasetapi.dto.request.CheckoutRequestDTO;
import com.example.datasetapi.dto.request.ConsumerBuyRequestDTO;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.CheckoutResponseDTO;
import com.example.datasetapi.dto.response.ConsumerBuyResponseDTO;
import com.example.datasetapi.dto.response.ConsumerSubResponseDTO;
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

    @PostMapping("/subRegister")
    public ResponseEntity<ApiResponse> buySub(@RequestParam long datasetSubPlanId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.subRegister(datasetSubPlanId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"sub buy loading success",consumerBuyResponseDTO));
    }

    @GetMapping("sub/mySub")
    public ResponseEntity<ApiResponse> subMySub(HttpServletRequest request){
        List<ConsumerSubResponseDTO> consumerSubscriptions = consumerService.getConsumerSubscriptions(request);
    return ResponseEntity.ok().body(new ApiResponse(true,"sub loading success",consumerSubscriptions));
    }

    @PostMapping("sub/select")
    public ResponseEntity<ApiResponse> selectSub(@ModelAttribute long consumerSubId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.selectSubPack(consumerSubId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"sub selecting success",consumerBuyResponseDTO));
    }
    @PostMapping("dowload/key")
    public ResponseEntity<ApiResponse> dowloadKey(@RequestParam long datasetId, HttpServletRequest request){
        return ResponseEntity.ok().body(new ApiResponse(true,"Dowload token ",null));
    }
    @PostMapping("group/buy")
    public ResponseEntity<ApiResponse> BuyGroup(@RequestParam long timeGroupId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyWithTimeGroup(timeGroupId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Dowload token ",consumerBuyResponseDTO));
    }
    @PostMapping("group/sub/buy")
    public ResponseEntity<ApiResponse> BuySubGroup(@RequestParam long timeGroupId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyTimeGroupWithSub(timeGroupId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"sub buy ",consumerBuyResponseDTO));
    }
    @PostMapping("apipack/buy")
    public ResponseEntity<ApiResponse> buyApiPack(@RequestParam long apiPackId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyApiPack(apiPackId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Dowload token ",consumerBuyResponseDTO));
    }



}
