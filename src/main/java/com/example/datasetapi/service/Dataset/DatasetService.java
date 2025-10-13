package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.dataset.Dataset;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public interface DatasetService {


    public void checkExitsAndCreateDatasetGroupAndDateset(ProviderUploadDatasetRequest providerUploadDatasetRequest,long Provider_id);


    ResponseEntity<ApiResponse> getAllCategories();

    ResponseEntity<ApiResponse> getAllDatasetType();


    ResponseEntity<?> acceptDataset(long datasetInforId, HttpServletRequest request);
    Dataset uploadCSVFileToPendingFolder(File file, Dataset dataset);

    ResponseEntity<?> getAllAllDataset();

    ResponseEntity<?> rejectDataset(long datasetInforId, HttpServletRequest request,String reason);
}
