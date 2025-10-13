package com.example.datasetapi.service.feature;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetType;
//import com.example.datasetapi.model.userManager.Address;
import com.example.datasetapi.model.location.Location;
import com.example.datasetapi.repository.LocationRepository;
import com.example.datasetapi.repository.DatasetInforRepository;
import com.example.datasetapi.repository.DatasetRepository;
import com.example.datasetapi.service.Dataset.DatasetService;
import com.example.datasetapi.service.Dataset.PriceService;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
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
    private LocationRepository  locationRepository;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private PriceService priceService;
    @Async
    public CompletableFuture<Map<String, Object>> readAndUploadDataset(ProviderUploadDatasetRequest providerUploadDatasetRequest, long provider_id, DatasetType datasetType) {
        try {

            System.out.println(" Bắt đầu đọc content...");
            DatasetInformation datasetInformation =
                    datasetInforRepository.findById(providerUploadDatasetRequest.getDataset_Information_Id())
                            .orElseThrow(() -> new RuntimeException("Dataset not found"));

            Optional<Location> addressOptional = locationRepository.findById(providerUploadDatasetRequest.getProvider_location_id());
            if(!addressOptional.isPresent()) {
                return  CompletableFuture.completedFuture(null);
            }
            datasetInformation.setLocation(addressOptional.get());



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
