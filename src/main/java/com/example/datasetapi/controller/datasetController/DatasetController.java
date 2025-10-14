package com.example.datasetapi.controller.datasetController;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.Dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("api/dataset")
public class DatasetController {
    @Autowired
    private DatasetService datasetService;

    @GetMapping("/categories/getAll")
    public ResponseEntity<ApiResponse> getAllDataset(){
        return datasetService.getAllCategories();
    }
    @GetMapping("/type/getAll")
    public ResponseEntity<ApiResponse> getAllDatasetType(){
        return datasetService.getAllDatasetType();
    }

    @GetMapping("/getAllParent")
    public ResponseEntity<?> getAllDatasetParent(){
        return datasetService.getAllDatasetParent();
    }
}
