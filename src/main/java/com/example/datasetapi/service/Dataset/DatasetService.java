package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface DatasetService {


    public void checkExitsAndCreateDatasetGroupAndDateset(ProviderUploadDatasetRequest providerUploadDatasetRequest,HttpServletRequest request);


    ResponseEntity<ApiResponse> getAllCategories();

    ResponseEntity<ApiResponse> getAllDatasetType();


}
