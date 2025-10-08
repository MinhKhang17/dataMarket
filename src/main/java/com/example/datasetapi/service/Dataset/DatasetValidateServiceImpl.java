package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.repository.*;

import com.example.datasetapi.model.Dataset.DatasetInformation;

import com.example.datasetapi.model.userManager.Provider;

import com.example.datasetapi.service.feature.AsyncDatasetService;
import com.example.datasetapi.service.feature.FileService;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;


@Service
public class DatasetValidateServiceImpl implements DatasetValidateService {


    private static final Set<String> CONNECTOR_ALLOWED = Set.of("CCS1", "CCS2", "CHAdeMO", "Type2", "GB/T");
    private static final Set<String> PRICING_MODEL_ALLOWED = Set.of("Flat", "Time-based", "Energy-based", "Subscription");


    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;

    @Autowired
  private   AsyncDatasetService asyncDatasetService;
    @Autowired
    private DatasetInforRepository datasetInforRepository;
    @Autowired
    private DatasetValidationErrorRepository datasetValidationErrorRepository;

    @Override
    public ResponseEntity<?> updateInforOfDatasetCheckContentUploadToCloud(ProviderUploadDatasetRequest providerUploadDatasetRequest,HttpServletRequest request) {

        Optional<DatasetInformation> datasetInformationOptional = datasetInforRepository.findById(providerUploadDatasetRequest.getDataset_Information_Id());

       //check xem dataset có tồn tại hay không
        if(!datasetInformationOptional.isPresent()){
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //check xem đã check header hay chưa
        if(!datasetInformationOptional.get().isHeaderChecked()){
            return ResponseEntity.badRequest().body(new ApiResponse(false,"The dataset hasn't been checked for headers.",null));
        }
        //check xem có thuộc về provider đó không
        if(datasetInformationOptional.get().getProvider().getId()!= tokenService.getUserIdFromRequest(request)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
            DatasetInformation datasetInformation = datasetInformationOptional.get();
        //call truoc de fetch day du thong tin
        datasetInformation.getDatasetType().getName();
        datasetInformation.getDatasetType().getDatasetTypeColumnList().get(0);

        CompletableFuture<Map<String, Object>> future =
                asyncDatasetService.readAndUploadDataset(providerUploadDatasetRequest, request,datasetInformation.getDatasetType());
        future.thenAccept(result -> {
            // callback khi async xong
            // them socket de gui thong bao den user
            System.out.println("Kết quả async: " + result);
        }).exceptionally(ex -> {
            System.err.println("Async bị lỗi: " + ex.getMessage());
            return null;
        });

        return ResponseEntity.ok(new ApiResponse(true,"Success in check content progress wait for moderator",datasetInformation.getId()));
    }
    @Override
    public ResponseEntity<?> uploadAndHeaderCheckCSVFile(MultipartFile file,long datasetTypeId, HttpServletRequest request) {
        try {
            //lay id tu request
            long provider_id = tokenService.getUserIdFromRequest(request);
            //lay provider de gan cho dataset
            Provider provider =userService.findProviderById(provider_id);
            //tao dataset infor de luu lỗi
            DatasetInformation ds = new DatasetInformation();
            //checkHeader
            boolean isChecked = fileService.checkHeader(file,datasetTypeId,ds,provider);

            if(!isChecked){
                return ResponseEntity.ok().body(new ApiResponse(false,"dataset header checked",ds));
            }
            //neu check thanh cong thi khoi tao dataset cho provider

            return ResponseEntity.ok().body(new ApiResponse(true,"check success",ds));

        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public ResponseEntity<?> getAllDatasetErrorWithDatasetInfor() {
        List<DatasetInformation> datasetInformations = datasetInforRepository.findAll();
        return null;
    }
}
