package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface DatasetValidateService {

    ResponseEntity<?> updateInforOfDatasetCheckContentUploadToCloud(ProviderUploadDatasetRequest providerUploadDatasetRequest, HttpServletRequest request);

    public ResponseEntity<?> uploadAndHeaderCheckCSVFile(MultipartFile file,long datasetTypeId, HttpServletRequest request);

    ResponseEntity<?> getAllDatasetErrorWithDatasetInfor();
}
