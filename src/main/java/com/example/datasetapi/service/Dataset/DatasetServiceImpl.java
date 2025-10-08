package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.model.Dataset.*;
import com.example.datasetapi.model.userManager.Address;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.repository.*;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DatasetServiceImpl implements DatasetService {


    //    @Autowired
//    private S3Client s3Client;
    @Autowired
    private DatasetRepository datasetRepository;
    @Autowired
    private DownloadTokenRepository dowloadTokenRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private DatasetTypeRepository datasetTypeRepository;
    @Autowired
    private DatasetValidateService datasetValidateService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private DatasetInforRepository datasetInforRepository;


    @Autowired
    private DatasetGroupRepository datasetGroupRepository;

    //    DatasetServiceImpl(S3Client s3Client, S3Config s3Config, DatasetRepository datasetRepository,DowloadTokenRepository dowloadTokenRepository) {
//    this.dowloadTokenRepository = dowloadTokenRepository;
//        this.s3Client = s3Client;
//        this.datasetRepository = datasetRepository;
//    }

    @Value("${aws.bucket.name}")
    private String BUCKET_NAME;


    @Override
    public ResponseEntity<ApiResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        if(categories.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(new ApiResponse(true,"load Category success",categories));
    }

    @Override
    public ResponseEntity<ApiResponse> getAllDatasetType() {
        List<DatasetType> datasetTypes = datasetTypeRepository.findAll();
        if(datasetTypes.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(new ApiResponse(true,"load DatasetType success",datasetTypes));
    }
    @Override
    public void checkExitsAndCreateDatasetGroupAndDateset(ProviderUploadDatasetRequest providerUploadDatasetRequest,HttpServletRequest request) {
        try {

            Provider provider = userService.findProviderById(tokenService.getUserIdFromRequest(request));

            Optional<DatasetInformation> datasetInformationOptional = datasetInforRepository.findById(providerUploadDatasetRequest.getDataset_Information_Id());

            if(!datasetInformationOptional.isPresent()){
                return;
            }
            DatasetInformation datasetInformation = datasetInformationOptional.get();
            // một user có nhiều địa chỉ upload tìm theo địa chỉ và dataset type
            Address address = userService.findProviderAddressByProviderIdAndAddressId(provider.getId(), providerUploadDatasetRequest.getProvider_address_id());
            //check xem đã tồn tại một dataset group chưa nếu chưa thì mặc định nó là lần đầu
            DatasetGroup datasetGroup = datasetGroupRepository.findByAddressAndDatasetType(address, datasetInformation.getDatasetType());

            Dataset dataset = new Dataset();

            //nếu là lần tạo đầu tiên thì tạo group để chứa các phiên bản
            if (datasetGroup == null) {
                datasetGroup = new DatasetGroup();
                datasetGroup.setDatasetType(datasetInformation.getDatasetType());
                datasetGroup.setAddress(address);
                datasetGroup.setProvider(provider);
            }
            //nếu là lần tạo thứ 2 tăng version của dataset group
            dataset.setDatasetStatus(DatasetStatus.PEDDING);
            dataset.setDatasetGroup(datasetGroup);
            dataset.setVersion(datasetGroup.getVersion() + 1);
            datasetGroup.setVersion(datasetGroup.getVersion()+1);


            //luu tam de test
            datasetRepository.save(dataset);
            datasetGroupRepository.save(datasetGroup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }





//    @Override
//    public Dataset uploadCSVFileToPendingFolder(MultipartFile file,Dataset dataset) {
//        try {
//
//            String fileName = file.getOriginalFilename();
//            String fileKey = "PENDING/"+UUID.randomUUID()+file.getOriginalFilename();
//
//            s3Client.putObject(PutObjectRequest.builder()
//                            .bucket(BUCKET_NAME)
//                            .key(fileKey)
//                            .build(),
//                    RequestBody.fromBytes(file.getBytes()));
//
//
//
//            dataset.setFileKey(fileKey);
//            dataset.setName(fileName);
//            dataset.setDatasetStatus(DatasetStatus.PEDDING);
//            return datasetRepository.save(dataset);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }



//    @Transactional
//    @Override
//    public ResponseEntity<?> dowloadDataset(String dowloadToken) {
//
//        //lay dowload token tu request checck xem nguoi dung co permussion de su dung hay khong
//        Optional<DownloadToken> downloadTokenOptional = dowloadTokenRepository.findById(UUID.fromString(dowloadToken));
//
//        //neu khong ton tai thi tra ve loi
//        if(!downloadTokenOptional.isPresent()){
//            return ResponseEntity.internalServerError().body(new ApiResponse(false,"token is not valid",null));
//        }
//
//        long datasetId = downloadTokenOptional.get().getDatasetId();
//
//        Optional<Dataset> datasetGetFromToken = datasetRepository.findById(datasetId);
//
//        if(!datasetGetFromToken.isPresent()){
//            return ResponseEntity.internalServerError().body(new ApiResponse(false,"can not find dataset with id + "+datasetId,null));
//        }
//
//
//        Dataset dataset = datasetGetFromToken.get();
//
//        String fileKey = dataset.getFileKey();
//
//        DownloadToken downloadToken = downloadTokenOptional.get();
//        downloadToken.setUsed(true);
//        dowloadTokenRepository.save(downloadToken);
//
//        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
//                .bucket(BUCKET_NAME)
//                .key(fileKey)
//                .build();
//
//        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
//        InputStreamResource resource = new InputStreamResource(s3Object);
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"" + Paths.get(fileKey).getFileName().toString() + "\"")
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(resource);
//    }


}
