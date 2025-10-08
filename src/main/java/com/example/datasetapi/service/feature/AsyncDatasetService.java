package com.example.datasetapi.service.feature;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.repository.DatasetInforRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncDatasetService {
    @Autowired
    private DatasetInforRepository datasetInforRepository;

    @Autowired
    private FileService fileService;


    @Async
    public CompletableFuture<Map<String, Object>> readAndUploadDataset(ProviderUploadDatasetRequest providerUploadDatasetRequest, HttpServletRequest request, DatasetType datasetType) {
        try {

            System.out.println(" Bắt đầu đọc content...");
            DatasetInformation datasetInformation =
                    datasetInforRepository.findById(providerUploadDatasetRequest.getDataset_Information_Id())
                            .orElseThrow(() -> new RuntimeException("Dataset not found"));


            Map<String, Object> result =
                    fileService.moderate(providerUploadDatasetRequest.getDataset_Information_Id(),datasetInformation,datasetType);


//            checkExitsAndCreateDatasetGroupAndDateset(providerUploadDatasetRequest,request);

//            datasetService.uploadCSVFileToPendingFolder(providerUploadDatasetRequest.getFile(), datasetInformation);

            System.out.println(" Upload thành công!");
            return  CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            System.err.println("Lỗi khi đọc/upload dataset: " + e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }


}
