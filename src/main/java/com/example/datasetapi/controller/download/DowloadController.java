package com.example.datasetapi.controller.download;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.dataset.DatasetService;
import com.example.datasetapi.service.user.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/dataset")
public class DowloadController {

    @Autowired
    private TokenService tokenService;
    @Autowired
    private DatasetService datasetService;

    DowloadController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }
    @GetMapping("/getKey")
    public ResponseEntity<ApiResponse> getKey(@RequestParam long datasetId,
                                              HttpServletRequest request) {
        return tokenService.getDownloadToken(datasetId,request);
        }

    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(
            @RequestParam String dowloadToken
    ) {

        return datasetService.downloadDataset(dowloadToken);

    }

//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadFile(MultipartFile file) throws IOException {
//
//        return datasetService.uploadFile(file);
//
//
//    }
}

