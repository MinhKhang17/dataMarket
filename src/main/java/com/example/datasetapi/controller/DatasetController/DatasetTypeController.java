package com.example.datasetapi.controller.DatasetController;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/type")
public class DatasetTypeController {
    @Autowired
    DatasetService datasetService;

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse> getAllDatasetType(){
        return datasetService.getAllDatasetType();
    }
}
