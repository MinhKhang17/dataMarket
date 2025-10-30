package com.example.datasetapi.controller.consumerController;

import com.example.datasetapi.dto.request.CheckoutRequestDTO;
import com.example.datasetapi.dto.request.ConsumerBuyRequestDTO;
import com.example.datasetapi.dto.response.*;

import com.example.datasetapi.service.dataset.DatasetService;
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
        return ResponseEntity.ok().body(new ApiResponse(true,"Checkout loading success",checkoutResponseDTO));
    }

    @PostMapping("dataset/buy")
    public ResponseEntity<ApiResponse> buyDataset(@ModelAttribute ConsumerBuyRequestDTO buyRequestDTO, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyDatasetRequest(buyRequestDTO,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Buy loading success",consumerBuyResponseDTO));
    }


    @PostMapping("/subRegister")
    public ResponseEntity<ApiResponse> buySub(@RequestParam long datasetSubPlanId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.subRegister(datasetSubPlanId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Sub buy loading success",consumerBuyResponseDTO));
    }

    @GetMapping("sub/mySub")
    public ResponseEntity<ApiResponse> subMySub(HttpServletRequest request){

        List<ConsumerSubResponseDTO> consumerSubscriptions = consumerService.getConsumerSubscriptions(request);
    return ResponseEntity.ok().body(new ApiResponse(true,"Sub loading success",consumerSubscriptions));
    }

    @PostMapping("sub/select")
    public ResponseEntity<ApiResponse> selectSub(@RequestParam long consumerSubId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.selectSubPack(consumerSubId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Sub selecting success",consumerBuyResponseDTO));
    }
    @GetMapping("dataset/key")
    public ResponseEntity<ApiResponse> dowloadKey(@RequestParam long datasetId, HttpServletRequest request){
       String download_token = datasetService.getDownloadTokenOfDatasetForConsumer(datasetId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Download token ",download_token));
    }
    @PostMapping("group/buy")
    public ResponseEntity<ApiResponse> BuyGroup(@RequestParam long timeGroupId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyWithTimeGroup(timeGroupId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Download token ",consumerBuyResponseDTO));
    }
    @PostMapping("group/sub/buy")
    public ResponseEntity<ApiResponse> BuySubGroup(@RequestParam long timeGroupId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyTimeGroupWithSub(timeGroupId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Sub buy ",consumerBuyResponseDTO));
    }
    @PostMapping("apipack/buy")
    public ResponseEntity<ApiResponse> buyApiPack(@RequestParam long apiPackId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyApiPack(apiPackId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Download token ",consumerBuyResponseDTO));
    }
    @GetMapping("dataset-history")
    public ResponseEntity<ApiResponse> datasetHistory(HttpServletRequest request){
        List<DatasetDTO> datasetDTOS = datasetService.findAllConsumerDataset(request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Consumer dataset ",datasetDTOS));
    }

//    @PostMapping("dataset/sub/buy")
//    public ResponseEntity<ApiResponse> buyDatasetWithSub(@RequestBody long datasetId,HttpServletRequest request){
//        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyDatasetWithSub(datasetId,request);
//    }

}
