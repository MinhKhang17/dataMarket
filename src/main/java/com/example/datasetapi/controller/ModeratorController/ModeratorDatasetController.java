package com.example.datasetapi.controller.ModeratorController;

import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetValidationError;
import com.example.datasetapi.service.Dataset.DatasetService;
import com.example.datasetapi.service.Dataset.DatasetValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/moderator/dataset")
public class ModeratorDatasetController {
    @Autowired
    private DatasetValidateService datasetValidateService;
    @GetMapping("/get-dataset-validation")
    public ResponseEntity<?> getAllDatasetInformation() {
        return datasetValidateService.getAllDatasetErrorWithDatasetInfor();
    }


}
