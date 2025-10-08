package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetValidationError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DatasetValidateService {

    ResponseEntity<?> updateInforOfDatasetCheckContentUploadToCloud(ProviderUploadDatasetRequest providerUploadDatasetRequest, HttpServletRequest request);

    public ResponseEntity<?> uploadAndHeaderCheckCSVFile(MultipartFile file,long datasetTypeId, HttpServletRequest request);

    ResponseEntity<?> getAllDatasetErrorWithDatasetInfor();
}
