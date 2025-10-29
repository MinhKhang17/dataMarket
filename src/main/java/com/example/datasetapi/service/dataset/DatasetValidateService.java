package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface DatasetValidateService {

//    ResponseEntity<?> updateInforOfDatasetCheckContentUploadToCloud(ProviderUploadDatasetRequest providerUploadDatasetRequest, HttpServletRequest request);

    public ResponseEntity<?> uploadAndHeaderCheckCSVFile(MultipartFile file, long datasetTypeId, HttpServletRequest request, ProviderUploadDatasetRequest providerUploadDatasetRequest, DatasetSourceType datasetProvider);

    ResponseEntity<?> getAllDatasetErrorWithDatasetInfor();
}
