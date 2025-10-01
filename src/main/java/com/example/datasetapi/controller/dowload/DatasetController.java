package com.example.datasetapi.controller.dowload;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.Dataset.DatasetService;
import com.example.datasetapi.service.user.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Paths;


@RestController
@RequestMapping("/api/dowload")
public class DatasetController {



    @Autowired
    private TokenService tokenService;
    @Autowired
    private DatasetService datasetService;

    DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }
    @GetMapping("/getKey")
    public ResponseEntity<ApiResponse> getKey(@RequestParam long datasetId,
                                              HttpServletRequest request) {
        return tokenService.getDownloadToken(datasetId,request);
        }

    @GetMapping("/api/download")
    public ResponseEntity<?> downloadFile(
            @RequestParam String dowloadToken
    ) {

        return datasetService.dowloadDataset(dowloadToken);

    }

//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadFile(MultipartFile file) throws IOException {
//
//        return datasetService.uploadFile(file);
//
//
//    }
}

