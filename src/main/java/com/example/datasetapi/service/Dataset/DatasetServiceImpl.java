package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.dto.request.CheckoutRequestDTO;
import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.enums.Datasets.DatasetGroupType;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.Mapper.DatasetMapper;
import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.enums.Datasets.DatasetInforStatus;
import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.model.Dataset.*;
//import com.example.datasetapi.model.userManager.Address;
import com.example.datasetapi.model.UserManager.Provider;
import com.example.datasetapi.model.UserManager.User;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.repository.*;
import com.example.datasetapi.service.payment.PaymentService;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.DateUtil;
import jakarta.servlet.http.HttpServletRequest;
import com.example.datasetapi.repository.DatasetRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
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
            List<DatasetParentReposonseDto> datasetReposonseDtoList = datasetGroupRepository.findAll()
                    .stream()
                    .map(datasetMapper::toDatasetParentReposonseDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok().body(new ApiResponse(true,"load dataset success",datasetReposonseDtoList));
    }

    @Autowired
    private DatasetRepository datasetRepository;
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
@Autowired
private PriceService priceService;
@Autowired
private CommuneRepository communeRepository;
@Autowired
TimeGroupRepository timeGroupRepository;
@Autowired
private PaymentService paymentService;
@Autowired
private  DatasetPricingRepository datasetPricingRepository;
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
    public void checkExitsAndCreateDatasetGroupAndDateset(ProviderUploadDatasetRequest providerUploadDatasetRequest, long provider_id, DatasetInformation datasetInformation) {
        try {
            logger.info("Check Exit and Create Dataset Group and Dateset");


            // một user có nhiều địa chỉ upload tìm theo địa chỉ và dataset type
            Commune commune = communeRepository.findById(providerUploadDatasetRequest.getCommune_id())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.COMMUNE_NOT_FOUND));

            Provider provider = userService.findProviderById(provider_id);

            //check xem đã tồn tại một dataset group parent chưa nếu chưa thì mặc định nó là lần đầu
            Optional<DatasetGroup> datasetGroupParentOptionnal = datasetGroupRepository.findByProviderAndDatasetGroupTypeAndProvinceAndDatasetType(provider,DatasetGroupType.PARENT, commune.getProvince(),datasetInformation.getDatasetType());
            DatasetGroup datasetGroupParent = new DatasetGroup();
            if(datasetGroupParentOptionnal.isEmpty()){
                  datasetGroupParent = new DatasetGroup();
                  datasetGroupParent.setDatasetGroupType(DatasetGroupType.PARENT);
                 datasetGroupParent.setDatasetType(datasetInformation.getDatasetType());
                 datasetGroupParent.setProvider(provider);
                 datasetGroupParent.setProvince(commune.getProvince());
                 datasetGroupParent.setUpdateAt(LocalDateTime.now());
                 datasetGroupParent = datasetGroupRepository.save(datasetGroupParent);
            }
            else {
                 datasetGroupParent = datasetGroupParentOptionnal.get();
                datasetGroupParent.setUpdateAt(LocalDateTime.now());
            }

            List<DatasetGroup> datasetGroupsChild = datasetGroupParent.getDatasetGroups();
            DatasetGroup datasetGroupFollowCommune = new DatasetGroup();
            boolean haveDatasetGroup = false;
            for( DatasetGroup datasetGroupChild : datasetGroupsChild ){
                if(datasetGroupChild.getCommune().equals(commune)){
                    datasetGroupFollowCommune = datasetGroupChild;
                    haveDatasetGroup = true;
                    break;
                }
            }

            Dataset dataset = new Dataset();
            //nếu là lần tạo đầu tiên thì tạo group để chứa các phiên bản
            if (!haveDatasetGroup) {
                datasetGroupFollowCommune = new DatasetGroup();
                datasetGroupFollowCommune.setDatasetGroupType(DatasetGroupType.CHILD);
                datasetGroupFollowCommune.setDatasetType(datasetInformation.getDatasetType());
                datasetGroupFollowCommune.setCommune(commune);
                datasetGroupFollowCommune.setProvider(provider);
                datasetGroupFollowCommune.setParent(datasetGroupParent);
                datasetGroupFollowCommune.getDatasets().add(dataset);
                datasetGroupParent.getDatasetGroups().add(datasetGroupFollowCommune);
                dataset.setDatasetChildGroup(datasetGroupFollowCommune);
                datasetGroupFollowCommune = datasetGroupRepository.save(datasetGroupFollowCommune);
            }
            else {
                //nếu là lần tạo thứ 2 thì chỉ cần gán dataset mới vào
                dataset.setDatasetStatus(DatasetStatus.PENDING);
                dataset.setDatasetChildGroup(datasetGroupFollowCommune);
                datasetGroupFollowCommune.getDatasets().add(dataset);
            }

            //thêm thời gian để truy xuất
            LocalDate datasetDate = DateUtil.parseToLocalDate(providerUploadDatasetRequest.getDataset_time());
            Optional<TimeGroup> timeGroupOptional = timeGroupRepository
                    .findByYearAndMonthAndDayAndDatasetGroupChildAndProvider(
                            datasetDate.getYear(),
                            datasetDate.getMonthValue(),
                            datasetDate.getDayOfMonth(),
                            datasetGroupFollowCommune,
                            provider
                    );

            TimeGroup timeGroup;
            if (timeGroupOptional.isPresent()) {
                timeGroup = timeGroupOptional.get();
            } else {
                timeGroup = TimeGroup.fromDate(datasetDate);
                timeGroup.setDatasetGroupChild(datasetGroupFollowCommune);
                timeGroup.setProvider(provider);
            }


            dataset.setProvider(provider);
            dataset.setTimeGroup(timeGroup);
            dataset.setDescription(providerUploadDatasetRequest.getDescription());
            dataset.setTitle(providerUploadDatasetRequest.getTitle());
            setDatasetPack(dataset,datasetInformation);
                logger.info("Đã chạy xong method check tồn tại vào datasetgroup");

            datasetGroupRepository.save(datasetGroupFollowCommune);
            datasetGroupRepository.save(datasetGroupParent);


//            //luu tam de test
            File file = new File(datasetInformation.getFile_url());
                uploadCSVFileToPendingFolder(file, dataset);

            System.out.println("Upload success!");
            datasetInformation.setDataset(dataset);
            datasetInformation.setDataset_time(DateUtil.parseToLocalDate(providerUploadDatasetRequest.getDataset_time()));
            datasetInforRepository.save(datasetInformation);
            datasetRepository.save(dataset);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private void setDatasetPack(Dataset dataset, DatasetInformation datasetInformation) {
        long dataset_row = datasetInformation.getRowCount();
        if(dataset_row<1000){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.DATASET_ROW_MIN_INVALID);
        }
        if(dataset_row >1000 && dataset_row <= 10000){
            dataset.setDatasetPack(DatasetPack.SMALL);
        }
        else if(dataset_row >10000 && dataset_row <= 100000){
            dataset.setDatasetPack(DatasetPack.MEDIUM);
        }
        else{
            dataset.setDatasetPack(DatasetPack.LARGE);
        }
    }

    @Override
    public ResponseEntity<?> acceptDataset(long datasetInforId, HttpServletRequest request) {
        Optional<DatasetInformation> datasetInformationOptional = datasetInforRepository.findById(datasetInforId);

        long moderator_id = tokenService.getUserIdFromRequest(request);

        if(datasetInformationOptional.isEmpty()){
            throw new CustomException(HttpStatus.NOT_FOUND,ErrorCode.DATASET_NOT_FOUND);
        }

        if(!datasetInformationOptional.get().getDataset().getDatasetStatus().equals(DatasetStatus.PENDING)){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.DATASET_NOT_PENDING);
        }

        if(!datasetInformationOptional.get().getStatus().equals(DatasetInforStatus.CONTENT_APPROVED)){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.DATASET_INFO_NOT_APPROVED);
        }

        Dataset dataset = datasetInformationOptional.get().getDataset();
        dataset.setDatasetStatus(DatasetStatus.APPROVE);
        datasetInformationOptional.get().setStatus(DatasetInforStatus.APPROVED);
        //cập nhật thông tin của datasetGroup
        DatasetGroup child = dataset.getDatasetChildGroup();
        DatasetGroup parent = dataset.getDatasetChildGroup().getParent();

        dataset.setVersion(child.getVersion()+1);
        child.setVersion(child.getVersion()+1);
        if(!parent.isHaveData()){
            parent.setHaveData(true);
        }

        datasetRepository.save(dataset);

        ReviewHistory reviewHistory = new ReviewHistory();
        reviewHistory.setDataset(dataset);
        reviewHistory.setModerator(userService.findUserById(moderator_id));
        reviewHistory.setProvider(datasetInformationOptional.get().getProvider());
        reviewHistoryRepository.save(reviewHistory);
        ReviewHistoryDto reviewHistoryDto = datasetMapper.toReviewHistoryDto(reviewHistoryRepository.save(reviewHistory));

        //tạo giá sau khi accept
        priceService.createPricingForDataset(dataset,datasetInformationOptional.get());

        return ResponseEntity.ok().body(new ApiResponse(true,"Dataset Accepted Successfully",reviewHistoryDto));
    }

    @Override
    public ResponseEntity<ApiResponse> rejectDataset(long datasetInforId,HttpServletRequest request,String reason) {

        long moderator_id = tokenService.getUserIdFromRequest(request);
        Optional<DatasetInformation> datasetInformation = datasetInforRepository.findById(datasetInforId);

        if(datasetInformation.isEmpty()){
            throw new CustomException(HttpStatus.NOT_FOUND,ErrorCode.DATASET_NOT_FOUND);
        }

        if(!datasetInformation.get().getDataset().getDatasetStatus().equals(DatasetStatus.PENDING)){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.DATASET_NOT_PENDING);
        }
        if(!datasetInformation.get().getStatus().equals(DatasetInforStatus.CONTENT_APPROVED)){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.DATASET_INFO_NOT_APPROVED);
        }

        ReviewHistory reviewHistory = new ReviewHistory();
        reviewHistory.setProvider(userService.findProviderById(datasetInformation.get().getProvider().getId()));
        reviewHistory.setDataset(datasetInformation.get().getDataset());
        reviewHistory.setModerator(userService.findUserById(moderator_id));
        reviewHistory.setReason(reason);
        datasetInformation.get().getDataset().setDatasetStatus(DatasetStatus.REJECT);
        datasetInformation.get().setStatus(DatasetInforStatus.CONTENT_REJECTED);
        return ResponseEntity.ok().body(new ApiResponse(true,"Dataset Reject Successfully",datasetMapper.toReviewHistoryDto(reviewHistoryRepository.save(reviewHistory))));
    }

    @Override
    public ResponseEntity<?> getAllDatasetParent() {
        List<DatasetParentReposonseDto> datasetReposonseDtoList = datasetGroupRepository.findByDatasetGroupType(DatasetGroupType.PARENT)
                .stream()
                .filter(datasetGroup -> datasetGroup.isHaveData())
                .map(datasetMapper::toDatasetParentReposonseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(new ApiResponse(true,"load dataset success",datasetReposonseDtoList));
    }

    @Override
    public DatasetParentReposonseDto getDatasetParentWithId(long datasetGroupId) {

        DatasetGroup temp =  datasetGroupRepository.findByIdAndDatasetGroupType(datasetGroupId,DatasetGroupType.PARENT).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.DATASET_NOT_FOUND));
        return datasetMapper.toDatasetParentReposonseDto(temp);
    }

    @Override
    public DatasetParentReposonseDto getDatasetParentDetailByDatasetId(long datasetId) {
        Dataset temp = datasetRepository.findById(datasetId).orElseThrow(()-> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.DATASET_NOT_FOUND));
        DatasetGroup datasetGroupOfDataset = temp.getDatasetChildGroup().getParent();
        return datasetMapper.toDatasetParentReposonseDto(datasetGroupOfDataset);
    }

    @Override
    public CheckoutResponseDTO checkoutDatasetPayment(CheckoutRequestDTO checkoutRequestDTO, HttpServletRequest request) {


        User consumer = userService.findUserById(tokenService.getUserIdFromRequest(request));

        CheckoutResponseDTO checkoutResponseDTO = new CheckoutResponseDTO();

        Dataset dataset = datasetRepository.findById(checkoutRequestDTO.getDatasetId()).orElseThrow(()-> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.DATASET_NOT_FOUND));
        DatasetDTO datasetDTO = datasetMapper.toDatasetForCheckoutDTO(dataset);

        DatasetPricing datasetPricing = datasetPricingRepository.findById(checkoutRequestDTO.getDatasetPricingId()).orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND,ErrorCode.DATASET_NOT_FOUND));

        DatasetPricingDTO datasetPricingDTO = datasetMapper.toDatasetPricingDTO(datasetPricing);

        checkoutResponseDTO.setDatasetPricing(datasetPricingDTO);
        checkoutResponseDTO.setDataset(datasetDTO);


        double remaing_amount = paymentService.calRemainingAmount(datasetPricingDTO.getPrice(),consumer);

        if(remaing_amount >= 0){
            checkoutResponseDTO.setEnough(true);
        }

        checkoutResponseDTO.setRemaining_amount(remaing_amount);

        return checkoutResponseDTO;
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
