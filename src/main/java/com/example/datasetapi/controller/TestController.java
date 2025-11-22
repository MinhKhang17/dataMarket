package com.example.datasetapi.controller;

import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.service.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("auth/test")
public class TestController {
    @Autowired
    private DatasetService datasetService;
    @PostMapping
    public Dataset uploadCSVFileToApproveFolder(@RequestBody MultipartFile file) {
        datasetService.uploadCSVFileToPendingFolder(file,new Dataset());
        return null;
    }
}
