package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.request.CheckoutRequestDTO;
import com.example.datasetapi.dto.request.ConsumerBuyRequestDTO;
import com.example.datasetapi.dto.request.ModeratorCreateNewDatasetGroupRequest;
import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.dto.service.DatasetGroupInfor;
import com.example.datasetapi.enums.Datasets.*;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.mapper.DatasetMapper;
import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.model.dataset.*;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.model.userManager.ConsumerSubscription;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.repository.*;
import com.example.datasetapi.service.feature.FileService;
import com.example.datasetapi.service.payment.PaymentService;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.DateUtil;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import com.example.datasetapi.repository.DatasetRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import software.amazon.awssdk.services.s3.model.*;

@Slf4j
@Service
public class DatasetServiceImpl implements DatasetService {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private DatasetPlanRepo datasetPlanRepo;
    @Autowired
    private WalletRepository walletRepository;
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
    @Autowired
    private ConsumerSubRepo consumerSubRepo;
    @Autowired private DownloadTokenRepository downloadTokenRepository;
    @Value("${aws.bucket.name}")
    private String BUCKET_NAME;

    private static final Logger logger =  LoggerFactory.getLogger(DatasetServiceImpl.class);
    @Autowired
    private FileService fileService;


    @Override
    public ResponseEntity<ApiResponse> searchDatasetByName(String datasetName) {
        if(datasetName==null || datasetName.trim().isEmpty()){
            List<Dataset> datasets = datasetRepository.findAll();
            return ResponseEntity.ok().body(new ApiResponse(true,"load all dataset success",
                    datasets.stream()
                            .map(datasetMapper::toDatasetDTO)
                            .collect(Collectors.toList())
            ));
        }
        List<Dataset> datasets = datasetRepository.searchByKeyword(datasetName);
        if(datasets.isEmpty()){
            return ResponseEntity.ok().body(new ApiResponse(false,"No Dataset found with the given name",datasetName));
        }
        return ResponseEntity.ok().body(new ApiResponse(true,"load Dataset success",
                datasets.stream()
                        .map(datasetMapper::toDatasetDTO)
                        .collect(Collectors.toList())
        ));
    }

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
    public void checkExitsAndCreateDatasetGroupAndDateset(
            ProviderUploadDatasetRequest request,
            long providerId,
            DatasetInformation datasetInformation,
            DatasetSourceType datasetSourceType) {

        try {
            logger.info("=== Start checking and creating Dataset Group and Dataset ===");

            Commune commune = communeRepository.findById(request.getCommune_id())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.COMMUNE_NOT_FOUND));

            // Khai báo mà không khởi tạo ngay để đảm bảo biến là effectively final
            Provider provider;
            User admin;

            // 🔹 Tìm hoặc tạo DatasetGroup parent theo sourceType
            DatasetGroup parentGroup = datasetGroupRepository
                    .findByDatasetGroupTypeAndProvinceAndDatasetTypeAndDatasetSourceType(
                            DatasetGroupType.PARENT,
                            commune.getProvince(),
                            datasetInformation.getDatasetType(),
                            datasetSourceType
                    )
                    .orElseGet(() -> createParentDatasetGroup(commune, datasetInformation, datasetSourceType));

            parentGroup.setUpdateAt(LocalDateTime.now());

            // 🔹 Tìm hoặc tạo DatasetGroup con theo commune + sourceType
            DatasetGroup childGroup = parentGroup.getDatasetGroups().stream()
                    .filter(group -> commune.equals(group.getCommune()) &&
                            group.getDatasetSourceType() == datasetSourceType)
                    .findFirst()
                    .orElseGet(() -> createChildDatasetGroup(parentGroup, commune, datasetInformation, datasetSourceType));

            // 🔹 Tạo dataset mới và set thông tin cơ bản
            Dataset dataset = new Dataset();
            dataset.setDatasetSourceType(datasetSourceType);
            dataset.setDatasetStatus(DatasetStatus.PENDING);
            dataset.setDatasetChildGroup(childGroup);
            dataset.setTitle(request.getTitle());
            dataset.setDescription(request.getDescription());
            dataset.setRow_count(datasetInformation.getRowCount());
            setDatasetPack(dataset, datasetInformation);
            if (datasetSourceType.equals(DatasetSourceType.DATASET_PROVIDER)) {
                admin = null;
                provider = userService.findProviderById(providerId);
                dataset.setProvider(provider);
            } else {
                provider = null;
                admin = userService.findUserById(providerId);
                dataset.setModerator(admin);
                dataset.setDatasetStatus(DatasetStatus.APPROVE);
                datasetInformation.setStatus(DatasetInforStatus.APPROVED);
                priceService.createPricingForDataset(dataset,datasetInformation);
            }



            // Nếu relationship được mapping 2 chiều, thêm dataset vào childGroup để persist quan hệ
            if (childGroup.getDatasets() == null) {
                childGroup.setDatasets(new ArrayList<>());
            }
            childGroup.getDatasets().add(dataset);

            // 🔹 Tìm hoặc tạo TimeGroup theo sourceType
            LocalDate datasetDate = DateUtil.parseToLocalDate(request.getDataset_time());
            TimeGroup timeGroup;

            if (datasetSourceType.equals(DatasetSourceType.DATASET_PROVIDER)) {
                // provider đã được gán ở trên
                timeGroup = timeGroupRepository
                        .findByYearAndMonthAndDatasetGroupChildAndProviderAndDatasetSourceType(
                                datasetDate.getYear(),
                                datasetDate.getMonthValue(),
                                childGroup,
                                provider,
                                datasetSourceType
                        )
                        .orElseGet(() -> createTimeGroup(datasetDate, childGroup, provider, datasetSourceType));
            } else {
                // moderator case: dùng admin (User)
                timeGroup = timeGroupRepository
                        .findByYearAndMonthAndDatasetGroupChildAndModeratorAndDatasetSourceType(
                                datasetDate.getYear(),
                                datasetDate.getMonthValue(),
                                childGroup,
                                admin,
                                datasetSourceType
                        )
                        .orElseGet(() -> createTimeGroup(datasetDate, childGroup, admin, datasetSourceType));
            }

            // đảm bảo row_Count không null trước khi cộng
            long existingRows = timeGroup.getRow_Count();

            timeGroup.setRow_Count(existingRows + datasetInformation.getRowCount());

            // Liên kết dataset <-> timeGroup
            dataset.setTimeGroup(timeGroup);
            dataset.setDatasetChildGroup(childGroup);

            // Liên kết datasetInformation
            datasetInformation.setDataset(dataset);
            datasetInformation.setDataset_time(datasetDate);
            dataset.setRow_count(datasetInformation.getRowCount());
            // (Upload file xử lý ở đây nếu cần — giữ nguyên như bạn muốn)
            if (datasetSourceType.equals(DatasetSourceType.DATASET_PROVIDER)) {
                // uploadCSVFileToPendingFolder(new File(datasetInformation.getFile_url()), dataset);
            } else {
                // uploadCSVFileToSytemFolder(new File(datasetInformation.getFile_url()), dataset);
            }

            logger.info(" Dataset [{}] uploaded successfully (source: {})", dataset.getTitle(), datasetSourceType);

            // Lưu dữ liệu (vì cascade có thể không set, lưu explicit dataset và timeGroup nếu cần)
            // Tùy mapping bạn có thể cần lưu timeGroup và childGroup; ở đây save dataset (cascades nếu cấu hình)
            datasetRepository.save(dataset);

            // Nếu timeGroup hoặc childGroup cần được lưu explicit (nếu không có cascade)
            timeGroupRepository.save(timeGroup);
            datasetGroupRepository.save(childGroup);
            datasetGroupRepository.save(parentGroup);

            logger.info("=== Completed checkExitsAndCreateDatasetGroupAndDateset ===");
        } catch (Exception e) {
            logger.error(" Error while processing dataset creation: {}", e.getMessage(), e);
            throw new RuntimeException("Error while creating dataset and groups", e);
        }
    }

    /* --- Các helper overloads đã được chỉnh — đặt trong cùng class/service nếu chưa có --- */

    private DatasetGroup createParentDatasetGroup(Commune commune, DatasetInformation info, DatasetSourceType datasetSourceType) {
        DatasetGroup parent = new DatasetGroup();
        parent.setDatasetGroupType(DatasetGroupType.PARENT);
        parent.setDatasetType(info.getDatasetType());
        parent.setProvince(commune.getProvince());
        parent.setUpdateAt(LocalDateTime.now());
        parent.setDatasetSourceType(datasetSourceType);
        return datasetGroupRepository.save(parent);
    }

    private DatasetGroup createChildDatasetGroup(DatasetGroup parent, Commune commune, DatasetInformation info, DatasetSourceType datasetSourceType) {
        DatasetGroup child = new DatasetGroup();
        child.setDatasetGroupType(DatasetGroupType.CHILD);
        child.setDatasetType(info.getDatasetType());
        child.setCommune(commune);
        child.setParent(parent);
        child.setDatasetSourceType(datasetSourceType);
        return datasetGroupRepository.save(child);
    }

    // overload cho Provider (owner)
    private TimeGroup createTimeGroup(LocalDate date, DatasetGroup group, Provider provider, DatasetSourceType datasetSourceType) {
        TimeGroup tg = TimeGroup.fromDate(date);
        tg.setDatasetGroupChild(group);
        tg.setProvider(provider);
        tg.setDatasetSourceType(datasetSourceType);
        return timeGroupRepository.save(tg);
    }

    // overload cho Moderator (User)
    private TimeGroup createTimeGroup(LocalDate date, DatasetGroup group, User moderator, DatasetSourceType datasetSourceType) {
        TimeGroup tg = TimeGroup.fromDate(date);
        tg.setDatasetGroupChild(group);
        tg.setModerator(moderator);
        tg.setDatasetSourceType(datasetSourceType);
        return timeGroupRepository.save(tg);
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
        datasetInformationOptional.get().setStatus(DatasetInforStatus.APPROVED);
        //cập nhật thông tin của datasetGroup
        DatasetGroup child = dataset.getDatasetChildGroup();
        DatasetGroup parent = dataset.getDatasetChildGroup().getParent();

        dataset.setVersion(child.getVersion()+1);
        child.setVersion(child.getVersion()+1);
        if(!parent.isHaveData()){
            parent.setHaveData(true);
        }
        dataset.setDatasetStatus(DatasetStatus.APPROVE);
        datasetRepository.save(dataset);

        ReviewHistory reviewHistory = new ReviewHistory();
        reviewHistory.setDataset(dataset);
        reviewHistory.setModerator(userService.findUserById(moderator_id));
        reviewHistory.setProvider(datasetInformationOptional.get().getDataset().getProvider());
        reviewHistoryRepository.save(reviewHistory);
        ReviewHistoryDto reviewHistoryDto = datasetMapper.toReviewHistoryDto(reviewHistoryRepository.save(reviewHistory));

//        moveFileFromPendingToApproveFolder(dataset);

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
        List<DatasetParentReposonseDto> datasetReposonseDtoList = datasetGroupRepository.findByDatasetGroupTypeAndDatasetSourceType(DatasetGroupType.PARENT,DatasetSourceType.SYSTEM_DATASET)
                .stream()
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

        System.out.println("dừng ở lần tìm đầu tiên");
//        Dataset dataset = datasetRepository.findById(8).get();
        Dataset dataset = datasetRepository.findById(checkoutRequestDTO.getDatasetId()).orElseThrow(()-> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.DATASET_NOT_FOUND));
        if(checkoutRequestDTO.getIsHaveSub()){
            System.out.println("vao được condition have sub");
            ConsumerSubscription consumerSubscription = consumerSubRepo.findByConsumerAndIsUsing(consumer,true).orElseThrow(
                    ()-> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.CONSUMER_SUB_NOT_FOUND)
            );

            checkoutResponseDTO.setDataset(datasetMapper.toDatasetForCheckoutDTO(dataset));

            checkoutResponseDTO.setRow_amount_consumer_sub(consumerSubscription.getRow_amount());

            checkoutResponseDTO.setRow_dataset(dataset.getRow_count());

        }
else {
            System.out.println("dừng ở lần tìm thứ hai");

            DatasetDTO datasetDTO = datasetMapper.toDatasetForCheckoutDTO(dataset);

            DatasetPricing datasetPricing = datasetPricingRepository.findById(checkoutRequestDTO.getDatasetPricingId()).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND));
            DatasetPricingDTO datasetPricingDTO = datasetMapper.toDatasetPricingDTO(datasetPricing);

            checkoutResponseDTO.setDatasetPricing(datasetPricingDTO);
            checkoutResponseDTO.setDataset(datasetDTO);


            double remaing_amount = paymentService.calRemainingAmount(datasetPricingDTO.getPrice(), consumer);

            if (remaing_amount >= 0) {
                checkoutResponseDTO.setEnough(true);
            }

            checkoutResponseDTO.setRemaining_amount(remaing_amount);
        }

        return checkoutResponseDTO;
    }

    @Override
    public ConsumerBuyResponseDTO buyDatasetRequest(ConsumerBuyRequestDTO buyRequestDTO, HttpServletRequest request) {
        Dataset dataset = datasetRepository.findById(buyRequestDTO.getDatasetId()).orElseThrow(()-> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.DATASET_NOT_FOUND));
        User consumer = userService.findUserById(tokenService.getUserIdFromRequest(request));

        if(buyRequestDTO.getIsHaveSub()){
            return createSubPayment(dataset,consumer);
        }
        else{
            DatasetPricing datasetPricing = datasetPricingRepository.findById(buyRequestDTO.getDatasetPricingId()).orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND,ErrorCode.DATASET_PRICING_NOT_FOUND));
            return createOneTimePayment(dataset,datasetPricing,consumer);
        }
    }

    @Override
    public ConsumerBuyResponseDTO subRegister(long pricingSubRuleId, HttpServletRequest request) {

        User consumer = userService.findUserById(tokenService.getUserIdFromRequest(request));

        PricingRule pricingRule = priceService.findSubPricingRuleById(pricingSubRuleId);

        if(consumerSubRepo.existsByPricingRuleAndConsumerAndIsActive(pricingRule,consumer,true)){
            throw new  CustomException(HttpStatus.BAD_REQUEST,ErrorCode.EXISTS_SUB);
        }

        paymentService.updateWallet(TransferType.TODOWN,Double.parseDouble(String.valueOf(pricingRule.getBasePricePoint())),tokenService.getUserIdFromRequest(request),BuyType.BUY_SUB);

        List<ConsumerSubscription> consumerSubscriptionList = consumerSubRepo.findAllByConsumerAndIsUsing(consumer,true);

        for(ConsumerSubscription consumerSubscription : consumerSubscriptionList){
            consumerSubscription.setUsing(false);
        }
        consumerSubRepo.saveAll(consumerSubscriptionList);

        ConsumerSubscription consumerSubscription = new ConsumerSubscription();
        consumerSubscription.setPricingRule(pricingRule);
        consumerSubscription.setConsumer(consumer);
        consumerSubscription.setExpiresAt(LocalDateTime.now().plusDays(pricingRule.getTimeLimitDay()));
        consumerSubscription.setSubType(pricingRule.getSubType());
        consumerSubscription.setRow_amount(pricingRule.getRowLimit());
        consumerSubscription.setActive(true);
        consumerSubscription.setUsing(true);


        return datasetMapper.toConsumerBuyResponseDTO(PricingMethod.SUBSCRIPTION,consumerSubRepo.save(consumerSubscription));

    }

    @Override
    public List<ConsumerSubscription> findConsumerSub(User consumer) {
        return consumerSubRepo.findByConsumer(consumer);
    }

    @Override
    public ConsumerBuyResponseDTO selectSubPack(long consumerSubId, HttpServletRequest request) {

        User consumer = userService.findUserById(tokenService.getUserIdFromRequest(request));
        ConsumerSubscription consumerSubscription = consumerSubRepo.findById(consumerSubId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND,ErrorCode.SUB_NOT_FOUND));

        consumerSubscription.setUsing(true);
        List<ConsumerSubscription> consumerSubscriptions = consumerSubRepo.findAllByConsumerAndIsUsing(consumer,true);
        for(ConsumerSubscription consumerSubscription1 : consumerSubscriptions){
            consumerSubscription1.setUsing(false);
        }
        consumerSubRepo.saveAll(consumerSubscriptions);
        return datasetMapper.toConsumerBuyResponseDTO(PricingMethod.SUBSCRIPTION,consumerSubRepo.save(consumerSubscription));
    }

    @Override
    public List<TimeGroupDTO> getAllTimeGroupFollowDatasetChildGroup(long datasetChildGroupId) {
        return timeGroupRepository.findAllByDatasetGroupChildId(datasetChildGroupId)
                .stream()
                .map(datasetMapper::toTimeGroupDTO)
                .collect(Collectors.toList());

    }

    @Override
    public List<DatasetDTO> findAllDatasetByTimeGroup(long datasetTimeGroupId) {
        return timeGroupRepository.findById(datasetTimeGroupId).
                orElseThrow(()-> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.DATASET_NOT_FOUND))
                .getDatasets()
                .stream()
                .map(datasetMapper::toDatasetDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConsumerBuyResponseDTO buyWithTimeGroup(long timeGroupId, HttpServletRequest request) {
        TimeGroup timeGroup = timeGroupRepository.findById(timeGroupId)        .orElseThrow(()-> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.TIME_GROUP_NOT_FOUND));

        double price = timeGroup.getPrice() ;
        System.out.println(price);
        User user = userService.findUserById(tokenService.getUserIdFromRequest(request));
        paymentService.updateWallet(TransferType.TODOWN,price,user.getId(),BuyType.BUY_WITH_TIME_GROUP);
        DownloadToken downloadToken = jwtUtil.generateDowloadToken(user,null,30,5,timeGroup);


        return datasetMapper.toConsumerBuyResponseDTO(PricingMethod.BUY_WITH_TIME_GROUP,downloadToken);
    }

    @Override
    public void saveTimeGroup(TimeGroup timeGroup) {
        timeGroupRepository.save(timeGroup);
    }




    private ConsumerBuyResponseDTO createSubPayment(Dataset dataset, User consumer) {
                ConsumerSubscription consumerSubscription = consumerSubRepo.findByConsumerAndIsUsing(consumer,true)
                        .orElseThrow(()-> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.SUB_NOT_FOUND));
            long dataset_row = dataset.getRow_count();

            if(consumerSubscription.getRow_amount()<dataset_row){
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.SUB_ROW_NOT_ENOUGH);
            }

            if (consumerSubscription.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.SUB_EXPIRED);
            }

            buyWithSubProcess(consumerSubscription,dataset_row);

        DownloadToken downloadToken = jwtUtil.generateDowloadToken(consumer,dataset,30,10,null);
            consumer.getDownloadTokens().add(downloadToken);
            userService.saveUser(consumer);
        ConsumerBuyResponseDTO consumerBuyResponseDTO =  datasetMapper.toConsumerBuyResponseDTO(PricingMethod.SUBSCRIPTION,consumerSubRepo.save(consumerSubscription));
        consumerBuyResponseDTO.getBuySubInfoDTO().setDowloadToken(downloadToken.getId().toString());
        return consumerBuyResponseDTO;
    }

    private ConsumerBuyResponseDTO createOneTimePayment(Dataset dataset, DatasetPricing datasetPricing, User consumer) {
        if(downloadTokenRepository.findByConsumerAndDatasetAndIsActive(consumer,dataset,true).isPresent()){
            throw new  CustomException(HttpStatus.BAD_REQUEST,ErrorCode.DATASET_BOUGHT);
        }
        DownloadToken downloadToken = jwtUtil.generateDowloadToken(consumer,dataset,30,2,null);
        consumer.getDownloadTokens().add(downloadToken);
        userService.saveUser(consumer);
        paymentService.updateWallet(TransferType.TODOWN,datasetPricing.getPrice(),consumer.getId(),BuyType.BUY_ONE_TIME_DATASET);
        return datasetMapper.toConsumerBuyResponseDTO(PricingMethod.ONE_TIME,downloadToken.getId());
    }


    @Override
    public Dataset uploadCSVFileToPendingFolder(File file, Dataset dataset) {
        String fileName = file.getName();
        String fileKey = "PENDING/" + UUID.randomUUID()  + fileName;

        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(fileKey)
                            .build(),
                    RequestBody.fromBytes(fileContent)
            );

            dataset.setFileKey(fileKey);
            dataset.setName(fileName);
            dataset.setDatasetStatus(DatasetStatus.PENDING);

            return datasetRepository.save(dataset);

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + fileName, e);
        } catch (S3Exception e) {
            throw new RuntimeException("Error uploading to S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }
    @Override
    public Dataset moveFileFromPendingToApproveFolder(Dataset dataset) {
        String oldKey = dataset.getFileKey(); // Ví dụ: PENDING/uuid/filename.csv
        if (oldKey == null || !oldKey.startsWith("PENDING/")) {
            throw new IllegalArgumentException("Dataset file is not in pending folder");
        }

        // Tạo key mới cho file trong folder APPROVE
        String fileName = oldKey.substring(oldKey.lastIndexOf("/") + 1);
        String newKey = "APPROVED/" + UUID.randomUUID()  + fileName;

        try {
            // 1️⃣ Copy từ PENDING sang APPROVE
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(BUCKET_NAME)
                    .sourceKey(oldKey)
                    .destinationBucket(BUCKET_NAME)
                    .destinationKey(newKey)
                    .build());

            // 2️⃣ Xóa file cũ trong PENDING
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(oldKey)
                    .build());

            // 3️⃣ Cập nhật dataset trong DB
            dataset.setFileKey(newKey);
            dataset.setDatasetStatus(DatasetStatus.APPROVE);
            return datasetRepository.save(dataset);

        } catch (S3Exception e) {
            throw new RuntimeException("Error moving file in S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public String getDownloadTokenOfDatasetForConsumer(long datasetId, HttpServletRequest request) {
        User user = userService.findUserById(tokenService.getUserIdFromRequest(request));
        Dataset dataset = findById(datasetId);
        Optional<DownloadToken> downloadTokenOptional = downloadTokenRepository.findByConsumerAndDatasetAndIsActive(user,dataset,true);
        if(downloadTokenOptional.isPresent()){
            if(downloadTokenOptional.get().getUse_amount() <= 0||downloadTokenOptional.get().getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.TOKEN_IS_EXPIRED);
            }
        }
        if(downloadTokenOptional.isEmpty()){
            throw new CustomException(HttpStatus.NOT_FOUND,ErrorCode.TOKEN_NOT_FOUND);
        }
        return downloadTokenOptional.get().getId().toString();
    }

    private Dataset findById(long datasetId) {
    return datasetRepository.findById(datasetId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.DATASET_NOT_FOUND));
    }

    @Override
    public ResponseEntity<?> getAllAllDataset() {
        List<DatasetParentReposonseDto> datasetReposonseDtoList = datasetGroupRepository.findAll()
                .stream()
                .map(datasetMapper::toDatasetParentReposonseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(new ApiResponse(true,"load dataset success",datasetReposonseDtoList));
    }

    @Override
    public ConsumerBuyResponseDTO buyTimeGroupWithSub(long timeGroupId, HttpServletRequest request) {
        TimeGroup timeGroup = timeGroupRepository.findById(timeGroupId).orElseThrow(()-> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.TIME_GROUP_NOT_FOUND));

        User consumer = userService.findUserById(tokenService.getUserIdFromRequest(request));

        ConsumerSubscription consumerSubscription = consumerSubRepo.findByConsumerAndIsUsing(consumer,true).orElseThrow(()-> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.SUB_NOT_FOUND));

        long timeGroupRowCount = timeGroup.getRow_Count();

        buyWithSubProcess(consumerSubscription,timeGroupRowCount);

        DownloadToken downloadToken = jwtUtil.generateDowloadToken(consumer,null,30,10,timeGroup);

        return datasetMapper.toConsumerBuyResponseDTO(PricingMethod.BUY_WITH_TIME_GROUP,downloadToken);
    }


    @Override
    public ConsumerBuyResponseDTO buyGroupByAPI(long timeGroupId, HttpServletRequest request) {
        return null;
    }

    @Override
    public ConsumerBuyResponseDTO buyApiPack(long apiPackId, HttpServletRequest request) {
        PricingRule pricingRule = priceService.findApiPricingRuleById(apiPackId);
        User user = userService.findUserById(tokenService.getUserIdFromRequest(request));
        double price = pricingRule.getBasePricePoint();

        paymentService.updateWallet(TransferType.TODOWN,price,user.getId(),BuyType.BUY_API);

        DownloadToken downloadToken = jwtUtil.generateDowloadToken(user,null,30,5,null);

        return datasetMapper.toConsumerBuyResponseDTO(PricingMethod.API,downloadToken);
    }

    public void buyWithSubProcess (ConsumerSubscription consumerSubscription,long rowCount){
            long rowCountConsumer = consumerSubscription.getRow_amount();

            if(rowCountConsumer<rowCount){
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.SUB_ROW_NOT_ENOUGH);
            }
            consumerSubscription.setRow_amount(rowCountConsumer-rowCount);
            consumerSubRepo.save(consumerSubscription);
    }
    @Transactional
    @Override
    public ResponseEntity<?> downloadDataset(String dowloadToken, HttpServletRequest request) {

        //lay dowload token tu request checck xem nguoi dung co permussion de su dung hay khong
        Optional<DownloadToken> downloadTokenOptional = downloadTokenRepository.findById(UUID.fromString(dowloadToken));
        User user = userService.findUserById(tokenService.getUserIdFromRequest(request));
        if(downloadTokenOptional.isEmpty()){
            throw new CustomException(HttpStatus.NOT_FOUND,ErrorCode.TOKEN_NOT_FOUND);
        }
        if(downloadTokenOptional.get().getConsumer()!= user) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }
        if(downloadTokenOptional.get().getUse_amount()==0){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.TOKEN_IS_EXPIRED);
        }
        Dataset dataset = downloadTokenOptional.get().getDataset();

        String fileKey = dataset.getFileKey();

        DownloadToken downloadToken = downloadTokenOptional.get();
        downloadToken.setUse_amount(downloadToken.getUse_amount()-1);
        if(downloadToken.getUse_amount()==0){
            downloadToken.setActive(false);
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(fileKey)
                .build();

        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
        InputStreamResource resource = new InputStreamResource(s3Object);
        dataset.setDowload_count(dataset.getDowload_count()+1);
        datasetRepository.save(dataset);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + Paths.get(fileKey).getFileName().toString() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Override
    public List<DatasetDTO> findAllConsumerDataset(HttpServletRequest request) {
        User user = userService.findUserById(tokenService.getUserIdFromRequest(request));
        return downloadTokenRepository.findByConsumerAndIsActive(user,true).stream()
                .map(DownloadToken::getDataset)
                .map(datasetMapper::toDatasetDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConsumerBuyResponseDTO buyDatasetWithSub(long datasetId, HttpServletRequest request) {
        User user = userService.findUserById(tokenService.getUserIdFromRequest(request));

        ConsumerSubscription consumerSubscription = consumerSubRepo.findByConsumerAndIsUsing(user,true).orElseThrow(
                ()-> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.SUB_NOT_FOUND)
        );
        return null;
    }

    @Override
    public void moderatorCreateNewDatasetGroup(ModeratorCreateNewDatasetGroupRequest moderatorCreateNewDatasetGroupRequest) {
        DatasetGroup parentDatasetGroup = new DatasetGroup();
        parentDatasetGroup.setDatasetType(datasetTypeRepository.findById(moderatorCreateNewDatasetGroupRequest.getDataset_type_id()).orElseThrow(
                () -> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.INVALID_TYPE_ID)
        ));

        DatasetGroup childDatasetGroup = new DatasetGroup();
        childDatasetGroup.setParent(parentDatasetGroup);
        parentDatasetGroup.setDatasetSourceType(DatasetSourceType.SYSTEM_DATASET);
        childDatasetGroup.setDatasetSourceType(DatasetSourceType.SYSTEM_DATASET);


        datasetGroupRepository.save(childDatasetGroup);
        datasetGroupRepository.save(parentDatasetGroup);
    }

    @Override
    public List<DatasetParentReposonseDto> findAllSystamDatasetGroup() {
        return datasetGroupRepository.findByDatasetGroupTypeAndDatasetSourceType(DatasetGroupType.PARENT,DatasetSourceType.SYSTEM_DATASET)
                .stream()
                .map(datasetMapper::toDatasetParentReposonseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DatasetParentReposonseDto> findAllProviderDataset() {
        return datasetGroupRepository.findAllByDatasetGroupTypeAndDatasetSourceType(DatasetGroupType.PARENT,DatasetSourceType.DATASET_PROVIDER)
                .stream()
                .map(datasetMapper::toDatasetParentReposonseDto)
                .collect(Collectors.toList());
    }

    @Override
    public DatasetGroupInfor getDatasetGroupInfor(long datasetGroupId) {
        DatasetGroup datasetGroup = datasetGroupRepository.findById(datasetGroupId).orElseThrow();
        return datasetMapper.toDatasetGroupInfor(datasetGroup);
    }

    @Override
    public Dataset uploadCSVFileToSytemFolder(File file, Dataset dataset) {
        String fileName = file.getName();
        String fileKey = "SYSTEM/" + UUID.randomUUID()  + fileName;

        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(fileKey)
                            .build(),
                    RequestBody.fromBytes(fileContent)
            );

            dataset.setFileKey(fileKey);
            dataset.setName(fileName);
            dataset.setDatasetStatus(DatasetStatus.PENDING);

            return datasetRepository.save(dataset);

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + fileName, e);
        } catch (S3Exception e) {
            throw new RuntimeException("Error uploading to S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }


}
