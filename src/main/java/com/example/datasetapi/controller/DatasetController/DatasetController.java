package com.example.datasetapi.controller.DatasetController;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.service.Dataset.DatasetValidateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class DatasetController {

    @Autowired
    private DatasetValidateService datasetValidateService;
    @PostMapping("/readByUrl")
    public ResponseEntity<?> uploadByUrl(
            @RequestParam Long datasetTypeId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String fileUrl
    ) {
        DatasetInformation ds = datasetValidateService.uploadAndSchemaCheckByUrl(datasetTypeId, fileUrl, name, description);
        return ResponseEntity.ok(ds);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(ProviderUploadDatasetRequest providerUploadDatasetRequest, HttpServletRequest request) {
        return datasetValidateService.uploadAndHeaderCheckCSVFile(providerUploadDatasetRequest,request);
    }
}
