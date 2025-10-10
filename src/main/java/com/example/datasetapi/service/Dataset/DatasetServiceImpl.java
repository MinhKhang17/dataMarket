package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.Mapper.DatasetMapper;
import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.DatasetReposonseDto;
import com.example.datasetapi.dto.response.ReviewHistoryDto;
import com.example.datasetapi.enums.Datasets.DatasetInforStatus;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.model.Dataset.*;
import com.example.datasetapi.model.userManager.Address;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.repository.*;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;

@Slf4j
@Service
public class DatasetServiceImpl implements DatasetService {


    @Override
    public ResponseEntity<?> getAllAllDataset() {
            List<DatasetReposonseDto> datasetReposonseDtoList = datasetGroupRepository.findAll()
                    .stream()
                    .map(datasetMapper::toDatasetReposonseDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok().body(new ApiResponse(true,"load dataset success",datasetReposonseDtoList));
    }

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
    private TokenService tokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private DatasetInforRepository datasetInforRepository;


    @Autowired
    private DatasetGroupRepository datasetGroupRepository;

    @Autowired
    private S3Client s3Client;
@Autowired
private ReviewHistoryRepository reviewHistoryRepository;

@Autowired
    private DatasetMapper datasetMapper;





    @Value("${aws.bucket.name}")
    private String BUCKET_NAME;


    private static final Logger logger =  LoggerFactory.getLogger(DatasetServiceImpl.class);


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



    @Transactional
    @Override
    public void checkExitsAndCreateDatasetGroupAndDateset(ProviderUploadDatasetRequest providerUploadDatasetRequest,long provider_id) {
        try {
            logger.info("Check Exit and Create Dataset Group and Dateset");

            Optional<DatasetInformation> datasetInformationOptional = datasetInforRepository.findById(providerUploadDatasetRequest.getDataset_Information_Id());

            if(!datasetInformationOptional.isPresent()){
                return;
            }
            DatasetInformation datasetInformation = datasetInformationOptional.get();
            // một user có nhiều địa chỉ upload tìm theo địa chỉ và dataset type
            Address address = userService.findProviderAddressByProviderIdAndAddressId(provider_id, providerUploadDatasetRequest.getProvider_address_id());
            //check xem đã tồn tại một dataset group chưa nếu chưa thì mặc định nó là lần đầu
            DatasetGroup datasetGroup = datasetGroupRepository.findByAddressAndDatasetType(address, datasetInformation.getDatasetType());

            Dataset dataset = new Dataset();
            Provider provider = userService.findProviderById(provider_id);
            //nếu là lần tạo đầu tiên thì tạo group để chứa các phiên bản
            if (datasetGroup == null) {
                logger.info("Lần tạo đầu tiên tạo datasetGroup");
                datasetGroup = new DatasetGroup();
                datasetGroup.setDatasetType(datasetInformation.getDatasetType());
                datasetGroup.setAddress(address);
                datasetGroup.setProvider(provider);
                datasetGroup.getDatasets().add(dataset);
            }
            else {
                //nếu là lần tạo thứ 2 tăng version của dataset group
                logger.info("Dataset group đã tồn tại không tạo mới");
                dataset.setDatasetStatus(DatasetStatus.PENDING);
                dataset.setDatasetGroup(datasetGroup);
                datasetGroup.getDatasets().add(dataset);
            }
            logger.info("Đã chạy xong method check tồn tại vào datasetgroup");
//            //luu tam de test
            File file = new File(datasetInformation.getFile_url());
                uploadCSVFileToPendingFolder(file, dataset);
            System.out.println(" Upload thành công!");
            datasetInformation.setDataset(dataset);
            datasetInforRepository.save(datasetInformation);
            datasetRepository.save(dataset);
            datasetGroupRepository.save(datasetGroup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public ResponseEntity<?> acceptDataset(long datasetInforId, HttpServletRequest request) {
        Optional<DatasetInformation> datasetInformationOptional = datasetInforRepository.findById(datasetInforId);

        long moderator_id = tokenService.getUserIdFromRequest(request);

        if(datasetInformationOptional.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(!datasetInformationOptional.get().getDataset().getDatasetStatus().equals(DatasetStatus.PENDING)){
            System.out.println("dataset khong pendding");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if(!datasetInformationOptional.get().getStatus().equals(DatasetInforStatus.CONTENT_APPROVED)){
            System.out.println("datasetInfor khong pendding");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Dataset dataset = datasetInformationOptional.get().getDataset();
        dataset.setDatasetStatus(DatasetStatus.APPROVE);
        datasetInformationOptional.get().setStatus(DatasetInforStatus.APPROVED);


        ReviewHistory reviewHistory = new ReviewHistory();
        reviewHistory.setDataset(dataset);
        reviewHistory.setModerator(userService.findUserById(moderator_id).get());
        reviewHistory.setProvider(datasetInformationOptional.get().getProvider());
        reviewHistoryRepository.save(reviewHistory);
        ReviewHistoryDto reviewHistoryDto = datasetMapper.toReviewHistoryDto(reviewHistoryRepository.save(reviewHistory));


        return ResponseEntity.ok().body(new ApiResponse(true,"Dataset Accepted Successfully",reviewHistoryDto));
    }
    @Override
    public ResponseEntity<ApiResponse> rejectDataset(long datasetInforId,HttpServletRequest request,String reason) {

        long moderator_id = tokenService.getUserIdFromRequest(request);
            Optional<DatasetInformation> datasetInformation = datasetInforRepository.findById(datasetInforId);

            if(datasetInformation.isEmpty()){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        if(!datasetInformation.get().getDataset().getDatasetStatus().equals(DatasetStatus.PENDING)){
            System.out.println("dataset khong pendding");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if(!datasetInformation.get().getStatus().equals(DatasetInforStatus.CONTENT_APPROVED)){
            System.out.println("datasetInfor khong pendding");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
            ReviewHistory reviewHistory = new ReviewHistory();
            reviewHistory.setProvider(userService.findProviderById(datasetInformation.get().getProvider().getId()));
            reviewHistory.setDataset(datasetInformation.get().getDataset());
            reviewHistory.setModerator(userService.findUserById(moderator_id).get());
            reviewHistory.setReason(reason);
            datasetInformation.get().getDataset().setDatasetStatus(DatasetStatus.REJECT);
            datasetInformation.get().setStatus(DatasetInforStatus.CONTENT_REJECTED);
        return ResponseEntity.ok().body(new ApiResponse(true,"Dataset Reject Successfully",datasetMapper.toReviewHistoryDto(reviewHistoryRepository.save(reviewHistory))));
    }


    @Override
    public Dataset uploadCSVFileToPendingFolder(File file, Dataset dataset) {

        String fileName = file.getName();
        String fileKey = "PENDING/"+ UUID.randomUUID()+fileName;

//            s3Client.putObject(PutObjectRequest.builder()
//                            .bucket(BUCKET_NAME)
//                            .key(fileKey)
//                            .build(),
//                    RequestBody.fromBytes(file.getBytes()));


        dataset.setFileKey(fileKey);
        dataset.setName(fileName);
        dataset.setDatasetStatus(DatasetStatus.PENDING);
        return datasetRepository.save(dataset);
    }



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
