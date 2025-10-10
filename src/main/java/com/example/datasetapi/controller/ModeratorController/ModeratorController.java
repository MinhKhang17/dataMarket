package com.example.datasetapi.controller.ModeratorController;

import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetValidationError;
import com.example.datasetapi.service.Dataset.DatasetService;
import com.example.datasetapi.service.Dataset.DatasetValidateService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/moderator/dataset")
public class ModeratorController {
    @Autowired
    private DatasetValidateService datasetValidateService;
    @Autowired
    private DatasetService datasetService;
    @GetMapping("/get-dataset-validation")
    public ResponseEntity<?> getAllDatasetInformation() {
        return datasetValidateService.getAllDatasetErrorWithDatasetInfor();
    }
    @PostMapping("/accept")
    public ResponseEntity<?> acceptDataset(@RequestParam long datasetInforId, HttpServletRequest request) {
            return datasetService.acceptDataset(datasetInforId,request);
    }
    @PostMapping("/reject")
    public ResponseEntity<?> rejectDataset(@RequestParam long datasetInforId, HttpServletRequest request,String reason) {
        return datasetService.rejectDataset(datasetInforId,request,reason);
    }


}
