package com.example.datasetapi.controller.provider;

import com.example.datasetapi.dto.request.LocationRegistrationRequest;
import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.service.dataset.DatasetService;
import com.example.datasetapi.service.dataset.DatasetValidateService;
import com.example.datasetapi.service.payment.PaymentService;
import com.example.datasetapi.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/provider")
public class providerController {
    @Autowired
    private DatasetValidateService datasetValidateService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private UserService userService;
    @Autowired
    private PaymentService paymentService;

    @PostMapping("dataset/validate-dataset")
    public ResponseEntity<?> checkDatasetContent(@RequestParam MultipartFile file,
                                                 @RequestParam long datasetTypeId,
                                                 HttpServletRequest request,
                                                 @ModelAttribute ProviderUploadDatasetRequest providerUploadDatasetRequest) {
        return datasetValidateService.uploadAndHeaderCheckCSVFile(file,datasetTypeId,request,providerUploadDatasetRequest, DatasetSourceType.DATASET_PROVIDER);
    }
//    @PostMapping("/validate-content-upload")
//    public ResponseEntity<?> validateContentUpload(ProviderUploadDatasetRequest providerUploadDatasetRequest, HttpServletRequest request) {
//        return datasetValidateService.updateInforOfDatasetCheckContentUploadToCloud(providerUploadDatasetRequest,request);
//    }
@PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("commune/get")
    public ResponseEntity<?> getCommune(HttpServletRequest request){
        return userService.getProviderCommune(request);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping("send-location-registration-request")
    public ResponseEntity<ApiResponse> sendLocationRegistrationRequest(@ModelAttribute LocationRegistrationRequest locationRegistrationRequest) {
        return userService.locationRegistrationProcess(locationRegistrationRequest);
    }
    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("revenue")
    public ResponseEntity<?> getProviderRevenue(HttpServletRequest request) {
        return ResponseEntity.ok().body(new ApiResponse(true,"load success",userService.getProviderRevenue(request)));
    }
    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("dataset/get")
    public  ResponseEntity<?> getDatasets(HttpServletRequest request) {
        return ResponseEntity.ok().body(datasetService.getAllProviderDataset(request));
    }
    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("dataset/detail/{id}")
    public ResponseEntity<?> getDataset(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok().body(datasetService.getDatasetDetail(id, request));
    }
    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping("dataset/cancel/{id}")
    public ResponseEntity<?> cancelDataset(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok().body(datasetService.cancelDataset(id,request));
    }

}
