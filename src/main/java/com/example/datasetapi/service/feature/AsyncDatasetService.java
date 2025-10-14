package com.example.datasetapi.service.feature;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetType;
//import com.example.datasetapi.model.userManager.Address;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.repository.CommuneRepository;
import com.example.datasetapi.repository.DatasetInforRepository;
import com.example.datasetapi.service.Dataset.DatasetService;
import com.example.datasetapi.service.Dataset.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncDatasetService {
    @Autowired
    private DatasetInforRepository datasetInforRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private DatasetService datasetService;
    @Autowired
    private PriceService priceService;
    @Autowired
    private CommuneRepository communeRepository;
    @Async
    public CompletableFuture<Map<String, Object>> readAndUploadDataset(ProviderUploadDatasetRequest providerUploadDatasetRequest, long provider_id, DatasetType datasetType) {
        try {

            System.out.println(" Bắt đầu đọc content...");
            DatasetInformation datasetInformation =
                    datasetInforRepository.findById(providerUploadDatasetRequest.getDataset_Information_Id())
                            .orElseThrow(() -> new CustomException(ErrorCode.DATASET_NOT_FOUND));

            Optional<Commune> addressOptional = communeRepository.findById(providerUploadDatasetRequest.getCommune_id());
            if(!addressOptional.isPresent()) {
                return  CompletableFuture.completedFuture(null);
            }
            datasetInformation.setCommune(addressOptional.get());



            Map<String, Object> result =
                    fileService.moderate(datasetInformation,datasetType);


                datasetService.checkExitsAndCreateDatasetGroupAndDateset(providerUploadDatasetRequest,provider_id);


            return  CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            System.err.println("Lỗi khi đọc/upload dataset: " + e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }


}
