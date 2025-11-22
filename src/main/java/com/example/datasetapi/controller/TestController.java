package com.example.datasetapi.controller;

import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.repository.DatasetRepository;
import com.example.datasetapi.service.dataset.DatasetService;
import com.example.datasetapi.service.feature.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("auth/test")
public class TestController {
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DatasetRepository datasetRepository;
    @PostMapping
    public Dataset uploadCSVFileToApproveFolder(@RequestBody MultipartFile file) {
        datasetService.uploadCSVFileToPendingFolder(file,new Dataset());
        return null;
    }
    @GetMapping("/api/dataset/{id}/json")
    public ResponseEntity<?> getDatasetAsJson(@PathVariable long id) {

        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dataset not found"));

        File csvFile = new File(dataset.getFileUrl());

        if (!csvFile.exists()) {
            throw new RuntimeException("CSV file not found");
        }

        try {
            List<Map<String, Object>> json = fileService.csvToJson(csvFile);
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error parsing CSV: " + e.getMessage());
        }
    }

}
