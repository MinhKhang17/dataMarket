package com.example.datasetapi.controller.consumerController;

import com.example.datasetapi.dto.request.CheckoutRequestDTO;
import com.example.datasetapi.dto.request.ConsumerBuyRequestDTO;
import com.example.datasetapi.dto.response.*;

import com.example.datasetapi.service.dataset.DatasetService;
import com.example.datasetapi.service.order.OrderService;
import com.example.datasetapi.service.user.ConsumerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/consumer/")
public class ConsumerController {
    @Autowired private DatasetService datasetService;
    @Autowired private ConsumerService consumerService;
    @Autowired private OrderService orderService;

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("dataset/checkout")
    public ResponseEntity<ApiResponse> checkout(
            @ModelAttribute CheckoutRequestDTO checkoutRequestDTO,
            @RequestParam(value = "isHaveSub", required = false) Boolean isHaveSub,
            HttpServletRequest request) {

            checkoutRequestDTO.setIsHaveSub(isHaveSub);


        CheckoutResponseDTO checkoutResponseDTO = datasetService.checkoutDatasetPayment(checkoutRequestDTO, request);
        return ResponseEntity.ok(new ApiResponse(true, "Checkout loading success", checkoutResponseDTO));
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @PostMapping("dataset/buy")
    public ResponseEntity<ApiResponse> buyDataset(@ModelAttribute ConsumerBuyRequestDTO buyRequestDTO,
                                                  @RequestParam(value = "isHaveSub", required = false) boolean isHaveSub,
                                                  HttpServletRequest request){
        buyRequestDTO.setIsHaveSub(isHaveSub);
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyDatasetRequest(buyRequestDTO,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Buy loading success",consumerBuyResponseDTO));
    }
    @PreAuthorize("hasRole('CONSUMER')")
    @PostMapping("/dataset/api/buying")
    public ResponseEntity<ApiResponse> buyAPiDataset(
            @ModelAttribute ConsumerBuyRequestDTO buyRequestDTO,
            HttpServletRequest request) {

        try {
            // set các thông tin từ query/params vào DTO nếu cần


            ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyAPIPack(buyRequestDTO, request);

            if (consumerBuyResponseDTO == null) {
                // service trả null -> coi là lỗi xử lý
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "Buy failed: internal error", null));
            }

            return ResponseEntity.ok(new ApiResponse(true, "Buy loading success", consumerBuyResponseDTO));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, iae.getMessage(), null));
        } catch (AccessDeniedException ade) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Access denied", null));
        } catch (Exception e) {
            // log lỗi ở đây nếu bạn có logger
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Unexpected error: " + e.getMessage(), null));
        }
    }


    @PreAuthorize("hasRole('CONSUMER')")
    @PostMapping("/subRegister")
    public ResponseEntity<ApiResponse> buySub(@RequestParam long datasetSubPlanId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.subRegister(datasetSubPlanId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Sub buy loading success",consumerBuyResponseDTO));
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("sub/mySub")
    public ResponseEntity<ApiResponse> subMySub(HttpServletRequest request){

        List<ConsumerSubResponseDTO> consumerSubscriptions = consumerService.getConsumerSubscriptions(request);
    return ResponseEntity.ok().body(new ApiResponse(true,"Sub loading success",consumerSubscriptions));
    }
    @PreAuthorize("hasRole('CONSUMER')")
    @PostMapping("sub/select")
    public ResponseEntity<ApiResponse> selectSub(@RequestParam long consumerSubId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.selectSubPack(consumerSubId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Sub selecting success",consumerBuyResponseDTO));
    }
    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("dataset/key")
    public ResponseEntity<ApiResponse> downloadKey(@RequestParam long datasetId, HttpServletRequest request){
       String download_token = datasetService.getDownloadTokenOfDatasetForConsumer(datasetId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Download token ",download_token));
    }
    @PreAuthorize("hasRole('CONSUMER')")
    @PostMapping("group/buy")
    public ResponseEntity<ApiResponse> BuyGroup(@RequestParam long timeGroupId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyWithTimeGroup(timeGroupId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Download token ",consumerBuyResponseDTO));
    }
//    @PreAuthorize("hasRole('CONSUMER')")
//    @PostMapping("group/sub/buy")
//    public ResponseEntity<ApiResponse> BuySubGroup(@RequestParam long timeGroupId, HttpServletRequest request){
//        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyTimeGroupWithSub(timeGroupId,request);
//        return ResponseEntity.ok().body(new ApiResponse(true,"Sub buy ",consumerBuyResponseDTO));
//    }

    @PreAuthorize("hasRole('CONSUMER')")
    @PostMapping("apipack/buy")
    public ResponseEntity<ApiResponse> buyApiPack(@RequestParam long apiPackId, HttpServletRequest request){
        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyApiPack(apiPackId,request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Download token ",consumerBuyResponseDTO));
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("dataset-history")
    public ResponseEntity<ApiResponse> datasetHistory(HttpServletRequest request){
        List<DatasetDTO> datasetDTOS = datasetService.findAllConsumerDataset(request);
        return ResponseEntity.ok().body(new ApiResponse(true,"Consumer dataset ",datasetDTOS));
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("order")
    public ResponseEntity<ApiResponse> viewOrderHistory(){
        return orderService.getOrdersForConsumer();
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("order/{orderId}")
    public ResponseEntity<ApiResponse> viewOrderById(@PathVariable Long orderId){
        return orderService.getOrderById(orderId);
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("api/token")
    public ResponseEntity<?> viewApiToken(){
        return  ResponseEntity.ok().body(datasetService.findAllTokenForConsumer());
    }
    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("api/token/detail")
    public ResponseEntity<?> viewApiTokenDetail(@RequestParam UUID token_id){
        return ResponseEntity.ok().body(datasetService.findTokenById(token_id));
    }
//    @PostMapping("dataset/sub/buy")
//    public ResponseEntity<ApiResponse> buyDatasetWithSub(@RequestBody long datasetId,HttpServletRequest request){
//        ConsumerBuyResponseDTO consumerBuyResponseDTO = datasetService.buyDatasetWithSub(datasetId,request);
//    }

}
