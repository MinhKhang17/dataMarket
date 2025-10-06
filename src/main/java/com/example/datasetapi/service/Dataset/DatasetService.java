package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.Dataset.Dataset;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface DatasetService {

    ResponseEntity<?> downloadDataset(String dowloadToken);

    Dataset uploadCSVFileToPendingFolder(MultipartFile file,Dataset dataset);
    ResponseEntity<ApiResponse> getAllCategories();

    ResponseEntity<ApiResponse> getAllDatasetType();

}
