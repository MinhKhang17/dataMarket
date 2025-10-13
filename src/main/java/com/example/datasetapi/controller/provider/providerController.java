package com.example.datasetapi.controller.provider;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.service.Dataset.DatasetValidateService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/provider/dataset")
public class providerController {
    @Autowired
    private DatasetValidateService datasetValidateService;
    @PostMapping("/validate-header-upload")
    public ResponseEntity<?> checkHeader(@RequestParam MultipartFile file, @RequestParam long datasetTypeId, HttpServletRequest request ) {
        return datasetValidateService.uploadAndHeaderCheckCSVFile(file,datasetTypeId,request);
    }
    @PostMapping("/validate-content-upload")
    public ResponseEntity<?> validateContentUpload(ProviderUploadDatasetRequest providerUploadDatasetRequest, HttpServletRequest request) {
        return datasetValidateService.updateInforOfDatasetCheckContentUploadToCloud(providerUploadDatasetRequest,request);
    }

}
