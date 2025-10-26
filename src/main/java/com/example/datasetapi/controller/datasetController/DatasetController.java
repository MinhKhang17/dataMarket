package com.example.datasetapi.controller.datasetController;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.dataset.DatasetService;
import com.example.datasetapi.dto.response.TimeGroupDTO;
import com.example.datasetapi.model.dataset.TimeGroup;
import com.example.datasetapi.service.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("api/auth/dataset")
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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchByDatasetName(@RequestParam(required = false) String name){
        return datasetService.searchDatasetByName(name);
    }

}
