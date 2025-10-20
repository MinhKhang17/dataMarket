package com.example.datasetapi.service.dataset;

import com.example.datasetapi.mapper.DatasetMapper;
import com.example.datasetapi.mapper.UserMapper;
import com.example.datasetapi.mapper.UserResponseDTOMapper;
import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.dto.response.ApiResponse;

import com.example.datasetapi.enums.Datasets.DatasetInforStatus;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.repository.*;

import com.example.datasetapi.model.dataset.DatasetInformation;

import com.example.datasetapi.model.userManager.Provider;

import com.example.datasetapi.service.feature.AsyncDatasetService;
import com.example.datasetapi.service.feature.FileService;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;

import com.example.datasetapi.util.Validator;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


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


    public UserResponseDTOMapper userResponseDTOMapper = new UserMapper();
    @Autowired
    private DatasetMapper datasetMapper;
    @Autowired
    private CommuneRepository communeRepository;
    @Autowired
    private DatasetService datasetService;

    private ResponseEntity<?> updateInforOfDatasetCheckContentUploadToCloud(ProviderUploadDatasetRequest providerUploadDatasetRequest,HttpServletRequest request,DatasetInformation ds) {
try {
    //check xem đã check header hay chưa
    if (!ds.isHeaderChecked()) {
        return ResponseEntity.badRequest().body(new ApiResponse(false, "The dataset hasn't been checked for headers.", null));
    }
    //check xem có thuộc về provider đó không
    if (ds.getProvider().getId() != tokenService.getUserIdFromRequest(request)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    long provider_id = tokenService.getUserIdFromRequest(request);


    if (!Validator.isValidLocalDate(providerUploadDatasetRequest.getDataset_time())) {
        throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.LOCAL_DATE_INVALID);
    }
    //call truoc de fetch day du thong tin
    ds.getDatasetType().getName();
    ds.getDatasetType().getDatasetTypeColumnList().get(0);
    ds.setUpdateAt(LocalDateTime.now());

    Optional<Commune> addressOptional = communeRepository.findById(providerUploadDatasetRequest.getCommune_id());
    if (!addressOptional.isPresent()) {
        throw new CustomException(HttpStatus.NOT_FOUND,ErrorCode.Location_NOT_FOUND);
    }
    ds.setCommune(addressOptional.get());
    Map<String, Object> result =
            fileService.moderate(ds);
        if(ds.getStatus()!= DatasetInforStatus.CONTENT_APPROVED){
            return ResponseEntity.badRequest().body(new ApiResponse(false, "The dataset hasn't been checked for headers.", result));
        }

    datasetService.checkExitsAndCreateDatasetGroupAndDateset(providerUploadDatasetRequest, provider_id,ds);

    return ResponseEntity.ok(new ApiResponse(true, "Success in check content progress wait for moderator", ds.getId()));
}catch (IllegalArgumentException e) {
    throw new RuntimeException(e);
}finally {
    try {

        System.out.println("-----------------------------------------\n" +
                "Deleted dataset\n" +
                "-----------------------------------------");
        Files.deleteIfExists(Paths.get(ds.getFile_url()));
    } catch (IOException e) {
        System.err.println("Can not delete current file: " + e.getMessage());
    }
}

    }
    @Override
    public ResponseEntity<?> uploadAndHeaderCheckCSVFile(MultipartFile file,long datasetTypeId, HttpServletRequest request, ProviderUploadDatasetRequest providerUploadDatasetRequest) {

            //lay id tu request
            long provider_id = tokenService.getUserIdFromRequest(request);
            //lay provider de gan cho dataset
            Provider provider =userService.findProviderById(provider_id);
            //tao dataset infor de luu lỗi
            DatasetInformation ds = new DatasetInformation();
            //checkHeader
            boolean isChecked = fileService.checkHeader(file,datasetTypeId,ds,provider);

            if(!isChecked){
                return ResponseEntity.badRequest().body(new ApiResponse(false,"dataset header checked and false",datasetMapper.toUploadHeaderResponseDto(ds)));
            }
            //neu check thanh cong thi chuyen sang check content dataset cho provider
            return updateInforOfDatasetCheckContentUploadToCloud(providerUploadDatasetRequest,request,ds);

    }
    @Override
    public ResponseEntity<?> getAllDatasetErrorWithDatasetInfor() {
        List<DatasetInformation> datasetInformationList = datasetInforRepository.findAll();
        datasetInformationList.forEach(datasetInformation -> {
            datasetInformation.getDatasetType().getName();
            datasetInformation.getProvider().getId();
            datasetInformation.getCommune().getName();
        });
        return ResponseEntity.ok().body(new ApiResponse(true,"Load success",datasetInformationList
                .stream()
                .filter(datasetInformation -> datasetInformation.getStatus()== DatasetInforStatus.CONTENT_APPROVED)
                .map(userResponseDTOMapper :: toModeratorDatasetInforResponseDto )
                .collect(Collectors.toList())
));
    }
}
