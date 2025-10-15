package com.example.datasetapi.controller.provider;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.service.Dataset.DatasetValidateService;
import com.example.datasetapi.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/provider")
public class providerController {
    @Autowired
    private DatasetValidateService datasetValidateService;

    @Autowired
    private UserService userService;

    @PostMapping("dataset/validate-dataset")
    public ResponseEntity<?> checkDatasetContent(@RequestParam MultipartFile file,
                                                 @RequestParam long datasetTypeId,
                                                 HttpServletRequest request,
                                                 @ModelAttribute ProviderUploadDatasetRequest providerUploadDatasetRequest) {
        return datasetValidateService.uploadAndHeaderCheckCSVFile(file,datasetTypeId,request,providerUploadDatasetRequest);
    }
//    @PostMapping("/validate-content-upload")
//    public ResponseEntity<?> validateContentUpload(ProviderUploadDatasetRequest providerUploadDatasetRequest, HttpServletRequest request) {
//        return datasetValidateService.updateInforOfDatasetCheckContentUploadToCloud(providerUploadDatasetRequest,request);
//    }

    @GetMapping("commune/get")
    public ResponseEntity<?> getCommune(HttpServletRequest request){
        return userService.getProviderCommune(request);
    }
}
