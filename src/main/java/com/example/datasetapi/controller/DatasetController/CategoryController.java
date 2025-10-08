package com.example.datasetapi.controller.DatasetController;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.Dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    @Autowired
    private DatasetService datasetService;

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse> getAllDataset(){
        return datasetService.getAllCategories();
    }
}
