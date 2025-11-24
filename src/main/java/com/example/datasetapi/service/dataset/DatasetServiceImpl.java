package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.request.*;
import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.dto.service.DatasetGroupInfor;
import com.example.datasetapi.enums.Datasets.*;
import com.example.datasetapi.enums.DowloadType;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.mapper.DatasetMapper;
import com.example.datasetapi.model.dataset.*;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.model.userManager.ConsumerSubscription;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.repository.*;
import com.example.datasetapi.service.feature.FileService;
import com.example.datasetapi.service.order.OrderService;
import com.example.datasetapi.service.payment.PaymentService;
import com.example.datasetapi.service.payment.TransactionService;
import com.example.datasetapi.service.payment.WalletService;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.DateUtil;
import com.example.datasetapi.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
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

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class DatasetServiceImpl implements DatasetService {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private JwtUtil jwtUtil;
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
    private DatasetGroupRepository datasetGroupRepository;
    @Autowired
    private ReviewHistoryRepository reviewHistoryRepository;
    @Autowired
    private DatasetMapper datasetMapper;
    @Autowired
    private PriceService priceService;
    @Autowired
    private CommuneRepository communeRepository;
    @Autowired
    private TimeGroupRepository timeGroupRepository;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private DatasetPricingRepository datasetPricingRepository;
    @Autowired
    private ConsumerSubRepo consumerSubRepo;
    @Autowired
    private DownloadTokenRepository downloadTokenRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ProvinceRepository provinceRepository;
    @Autowired
    private FileService fileService;
    @Autowired
    private DatasetPlanRepo datasetPlanRepo;

    @Value("${app.upload.base}")
    private String UPLOAD_BASE;
    private static final Logger logger = LoggerFactory.getLogger(DatasetServiceImpl.class);

    @PostConstruct
    public void initUploadDirs() {
        try {
            Path base = Paths.get(UPLOAD_BASE).toAbsolutePath();
            Files.createDirectories(base.resolve("PENDING"));
            Files.createDirectories(base.resolve("APPROVED"));
            Files.createDirectories(base.resolve("SYSTEM"));
            Files.createDirectories(base.resolve("TEMP"));

            logger.info("Upload directories initialized at: {}", base);
        } catch (IOException e) {
            logger.error("Failed to create upload directories", e);
            throw new RuntimeException("Failed to create upload directories", e);
        }
    }

    @Override
    public ResponseEntity<ApiResponse> searchDatasetByName(String datasetName) {
        if (datasetName == null || datasetName.trim().isEmpty()) {
            List<Dataset> datasets = datasetRepository.findAll();
            return ResponseEntity.ok().body(new ApiResponse(true, "load all dataset success",
                    datasets.stream().map(datasetMapper::toDatasetDTO).collect(Collectors.toList())));
        }
        List<Dataset> datasets = datasetRepository.searchByKeyword(datasetName);
        if (datasets.isEmpty()) {
            return ResponseEntity.ok().body(new ApiResponse(false, "No Dataset found with the given name", datasetName));
        }
        return ResponseEntity.ok().body(new ApiResponse(true, "load Dataset success",
                datasets.stream().map(datasetMapper::toDatasetDTO).collect(Collectors.toList())));
    }

    @Override
    public ResponseEntity<ApiResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(new ApiResponse(true, "load Category success", categories));
    }

    @Override
    public ResponseEntity<ApiResponse> getAllDatasetType() {
        List<DatasetType> datasetTypes = datasetTypeRepository.findAll();
        if (datasetTypes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(new ApiResponse(true, "load DatasetType success", datasetTypes));
    }

    @Override
    public void checkExitsAndCreateDatasetGroupAndDateset(
            ProviderUploadDatasetRequest request,
            long providerId,
            Dataset dataset,
            DatasetSourceType datasetSourceType, MultipartFile fileFromRequest) {

        try {
            logger.info("=== Start checking and creating Dataset Group and Dataset ===");

            Commune commune = communeRepository.findById(request.getCommune_id())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.COMMUNE_NOT_FOUND));

            DatasetType datasetType = dataset.getDatasetType();

            DatasetGroup parentGroup = datasetGroupRepository
                    .findByDatasetGroupTypeAndProvinceAndDatasetTypeAndDatasetSourceType(
                            DatasetGroupType.PARENT,
                            commune.getProvince(),
                            datasetType,
                            datasetSourceType
                    )
                    .orElseGet(() -> {
                        DatasetGroup newParent = createParentDatasetGroup(commune, datasetType, datasetSourceType);
                        return datasetGroupRepository.saveAndFlush(newParent);
                    });

            parentGroup.setUpdateAt(LocalDateTime.now());

            DatasetGroup childGroup = datasetGroupRepository
                    .findByParentAndCommuneAndDatasetSourceType(parentGroup, commune, datasetSourceType)
                    .orElseGet(() -> {
                        DatasetGroup newChild = createChildDatasetGroup(parentGroup, commune, datasetType, datasetSourceType);
                        newChild.setParent(parentGroup);
                        return datasetGroupRepository.saveAndFlush(newChild);
                    });

            final Provider provider;
            final User admin;

            dataset.setDatasetSourceType(datasetSourceType);
            dataset.setDatasetStatus(DatasetStatus.PENDING);
            dataset.setDatasetChildGroup(childGroup);
            dataset.setTitle(request.getTitle());
            dataset.setDescription(request.getDescription());
            dataset.setRowCount(dataset.getRowCount());
            dataset.setCommune(commune);

            setDatasetPack(dataset);

            if (datasetSourceType.equals(DatasetSourceType.DATASET_PROVIDER)) {
                provider = userService.findProviderByUserId(providerId);
                admin = null;
                dataset.setProvider(provider);
            } else {
                admin = userService.findUserById(providerId);
                provider = null;
                dataset.setModerator(admin);
                dataset.setDatasetStatus(DatasetStatus.APPROVE);
            }

            dataset = datasetRepository.saveAndFlush(dataset);

            if (!datasetSourceType.equals(DatasetSourceType.DATASET_PROVIDER)) {
                priceService.createPricingForDataset(dataset, request);
            }

            LocalDate datasetDate = DateUtil.parseToLocalDate(request.getDataset_time());
            final TimeGroup timeGroup;

            if (datasetSourceType.equals(DatasetSourceType.DATASET_PROVIDER)) {
                timeGroup = timeGroupRepository
                        .findByYearAndMonthAndDatasetGroupChildAndProviderAndDatasetSourceType(
                                datasetDate.getYear(),
                                datasetDate.getMonthValue(),
                                childGroup,
                                provider,
                                datasetSourceType
                        )
                        .orElseGet(() -> {
                            TimeGroup newTimeGroup = createTimeGroup(datasetDate, childGroup, provider, datasetSourceType);
                            return timeGroupRepository.saveAndFlush(newTimeGroup);
                        });
            } else {
                timeGroup = timeGroupRepository
                        .findByYearAndMonthAndDatasetGroupChildAndModeratorAndDatasetSourceType(
                                datasetDate.getYear(),
                                datasetDate.getMonthValue(),
                                childGroup,
                                admin,
                                datasetSourceType
                        )
                        .orElseGet(() -> {
                            TimeGroup newTimeGroup = createTimeGroup(datasetDate, childGroup, admin, datasetSourceType);
                            return timeGroupRepository.saveAndFlush(newTimeGroup);
                        });
            }

            long existingRows = timeGroup.getRow_Count() != 0 ? timeGroup.getRow_Count() : 0L;
            timeGroup.setRow_Count(existingRows + dataset.getRowCount());

            dataset.setTimeGroup(timeGroup);
            dataset.setDatasetTime(datasetDate);

//            File sourceFile = new File(dataset.getFileUrl());
            Dataset datasetWithFileInfo;

            if (datasetSourceType.equals(DatasetSourceType.DATASET_PROVIDER)) {
                datasetWithFileInfo = uploadCSVFileToPendingFolder(fileFromRequest, dataset);
            } else {
                datasetWithFileInfo = uploadCSVFileToSytemFolder(fileFromRequest, dataset);
            }

            dataset.setFileKey(datasetWithFileInfo.getFileKey());
            dataset.setFileUrl(datasetWithFileInfo.getFileUrl());

            logger.info("Dataset [{}] uploaded successfully (source: {})", dataset.getTitle(), datasetSourceType);

            timeGroupRepository.saveAndFlush(timeGroup);
            datasetRepository.saveAndFlush(dataset);
            datasetGroupRepository.saveAndFlush(childGroup);
            datasetGroupRepository.saveAndFlush(parentGroup);

            logger.info("=== Completed checkExitsAndCreateDatasetGroupAndDateset ===");

        } catch (Exception e) {
            logger.error("Error while processing dataset creation: {}", e.getMessage(), e);
            throw new RuntimeException("Error while creating dataset and groups", e);
        }
//        finally {
//            try {
//
//                System.out.println("-----------------------------------------\n" +
//                        "Deleted dataset\n" +
//                        "-----------------------------------------");
//                Files.deleteIfExists(Paths.get(UPLOAD_BASE+"TEMP"+dataset.getName()));
//            } catch (IOException e) {
//                System.err.println("Can not delete current file: " + e.getMessage());
//            }
//        }
    }
    private DatasetGroup createParentDatasetGroup(Commune commune, DatasetType datasetType, DatasetSourceType datasetSourceType) {
        DatasetGroup parent = new DatasetGroup();
        parent.setDatasetGroupType(DatasetGroupType.PARENT);
        parent.setDatasetType(datasetType);
        parent.setProvince(commune.getProvince());
        parent.setUpdateAt(LocalDateTime.now());
        parent.setDatasetSourceType(datasetSourceType);
        return datasetGroupRepository.save(parent);
    }

    private DatasetGroup createChildDatasetGroup(DatasetGroup parent, Commune commune, DatasetType datasetType, DatasetSourceType datasetSourceType) {
        DatasetGroup child = new DatasetGroup();
        child.setDatasetGroupType(DatasetGroupType.CHILD);
        child.setDatasetType(datasetType);
        child.setCommune(commune);
        child.setParent(parent);
        child.setDatasetSourceType(datasetSourceType);
        return datasetGroupRepository.save(child);
    }

    private TimeGroup createTimeGroup(LocalDate date, DatasetGroup group, Provider provider, DatasetSourceType datasetSourceType) {
        TimeGroup tg = TimeGroup.fromDate(date);
        tg.setDatasetGroupChild(group);
        tg.setProvider(provider);
        tg.setDatasetSourceType(datasetSourceType);
        return timeGroupRepository.save(tg);
    }

    private TimeGroup createTimeGroup(LocalDate date, DatasetGroup group, User moderator, DatasetSourceType datasetSourceType) {
        TimeGroup tg = TimeGroup.fromDate(date);
        tg.setDatasetGroupChild(group);
        tg.setModerator(moderator);
        tg.setDatasetSourceType(datasetSourceType);
        return timeGroupRepository.save(tg);
    }

    private void setDatasetPack(Dataset dataset) {
        long datasetRow = dataset.getRowCount();
        if(datasetRow < 1000){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.DATASET_ROW_MIN_INVALID);
        }
        if(datasetRow > 1000 && datasetRow <= 10000){
            dataset.setDatasetPack(DatasetPack.SMALL);
        }
        else if(datasetRow > 10000 && datasetRow <= 100000){
            dataset.setDatasetPack(DatasetPack.MEDIUM);
        }
        else{
            dataset.setDatasetPack(DatasetPack.LARGE);
        }
    }

    @Override
    public ResponseEntity<?> acceptDataset(long datasetId, HttpServletRequest request) {
        Optional<Dataset> datasetOptional = datasetRepository.findById(datasetId);
        long moderatorId = tokenService.getUserIdFromRequest(request);

        if(datasetOptional.isEmpty()){
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND);
        }

        Dataset dataset = datasetOptional.get();

        if(!dataset.getDatasetStatus().equals(DatasetStatus.PENDING)){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.DATASET_NOT_PENDING);
        }

        if(!dataset.isContentChecked()){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.DATASET_INFO_NOT_APPROVED);
        }

        DatasetGroup child = dataset.getDatasetChildGroup();
        DatasetGroup parent = dataset.getDatasetChildGroup().getParent();

        child.setVersion(child.getVersion() + 1);
        child.setUpdateAt(LocalDateTime.now());

        if(!parent.getIsHaveData()){
            parent.setIsHaveData(true);
        }
        parent.setUpdateAt(LocalDateTime.now());

        dataset.setDatasetStatus(DatasetStatus.APPROVE);

        datasetGroupRepository.save(parent);
        datasetGroupRepository.save(child);
        datasetRepository.save(dataset);

        ReviewHistory reviewHistory = new ReviewHistory();
        reviewHistory.setDataset(dataset);
        reviewHistory.setModerator(userService.findUserById(moderatorId));
        reviewHistory.setProvider(dataset.getProvider());
        reviewHistory = reviewHistoryRepository.save(reviewHistory);

        moveFileFromPendingToApproveFolder(dataset);

        priceService.createPricingForDataset(dataset, new ProviderUploadDatasetRequest());
        priceService.createRevenueForProvider(dataset.getProvider(), dataset.getDatasetPack(), dataset);

        return ResponseEntity.ok().body("Accept success");
    }

    @Override
    public ResponseEntity<ApiResponse> rejectDataset(long datasetId, HttpServletRequest request, String reason) {
        long moderatorId = tokenService.getUserIdFromRequest(request);
        Optional<Dataset> datasetOptional = datasetRepository.findById(datasetId);

        if(datasetOptional.isEmpty()){
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND);
        }

        Dataset dataset = datasetOptional.get();

        if(!dataset.getDatasetStatus().equals(DatasetStatus.PENDING)){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.DATASET_NOT_PENDING);
        }

        if(!dataset.isContentChecked()){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.DATASET_INFO_NOT_APPROVED);
        }

        ReviewHistory reviewHistory = new ReviewHistory();
        reviewHistory.setProvider(userService.findProviderById(dataset.getProvider().getId()));
        reviewHistory.setDataset(dataset);
        reviewHistory.setModerator(userService.findUserById(moderatorId));
        reviewHistory.setReason(reason);
        dataset.setDatasetStatus(DatasetStatus.REJECT);

        datasetRepository.save(dataset);

        return ResponseEntity.ok().body(new ApiResponse(true, "Dataset Reject Successfully",
                datasetMapper.toReviewHistoryDto(reviewHistoryRepository.save(reviewHistory))));
    }

    @Override
    public ResponseEntity<?> getAllDatasetParent() {
        List<DatasetParentReposonseDto> datasetReposonseDtoList = datasetGroupRepository
                .findByDatasetGroupTypeAndDatasetSourceType(DatasetGroupType.PARENT, DatasetSourceType.SYSTEM_DATASET)
                .stream()
                .map(datasetMapper::toDatasetParentReposonseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(new ApiResponse(true, "load dataset success", datasetReposonseDtoList));
    }

    @Override
    public DatasetParentReposonseDto getDatasetParentWithId(long datasetGroupId) {
        DatasetGroup temp = datasetGroupRepository.findByIdAndDatasetGroupType(datasetGroupId, DatasetGroupType.PARENT)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND));
        return datasetMapper.toDatasetParentReposonseDto(temp);
    }

    @Override
    public DatasetParentReposonseDto getDatasetParentDetailByDatasetId(long datasetId) {
        Dataset temp = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND));
        DatasetGroup datasetGroupOfDataset = temp.getDatasetChildGroup().getParent();
        return datasetMapper.toDatasetParentReposonseDto(datasetGroupOfDataset);
    }

    @Override
    public CheckoutResponseDTO checkoutDatasetPayment(CheckoutRequestDTO checkoutRequestDTO, HttpServletRequest request) {
        User consumer = userService.findUserById(tokenService.getUserIdFromRequest(request));
        CheckoutResponseDTO checkoutResponseDTO = new CheckoutResponseDTO();

        Dataset dataset = datasetRepository.findById(checkoutRequestDTO.getDatasetId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND));

        if(checkoutRequestDTO.getIsHaveSub()){
            ConsumerSubscription consumerSubscription = consumerSubRepo.findByConsumerAndIsUsing(consumer, true)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.CONSUMER_SUB_NOT_FOUND));

            checkoutResponseDTO.setDataset(datasetMapper.toDatasetForCheckoutDTO(dataset));
            checkoutResponseDTO.setRow_amount_consumer_sub(consumerSubscription.getRow_amount());
            checkoutResponseDTO.setRow_dataset(dataset.getRowCount());
        } else {
            DatasetDTO datasetDTO = datasetMapper.toDatasetForCheckoutDTO(dataset);
            DatasetPricing datasetPricing = datasetPricingRepository.findById(checkoutRequestDTO.getDatasetPricingId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND));
            DatasetPricingDTO datasetPricingDTO = datasetMapper.toDatasetPricingDTO(datasetPricing);

            checkoutResponseDTO.setDatasetPricing(datasetPricingDTO);
            checkoutResponseDTO.setDataset(datasetDTO);

            double remaingAmount = paymentService.calRemainingAmount(datasetPricingDTO.getPrice(), consumer);
            if (remaingAmount >= 0) {
                checkoutResponseDTO.setEnough(true);
            }
            checkoutResponseDTO.setRemaining_amount(remaingAmount);
        }

        return checkoutResponseDTO;
    }

    @Override
    public ConsumerBuyResponseDTO buyDatasetRequest(ConsumerBuyRequestDTO buyRequestDTO, HttpServletRequest request) {
        Dataset dataset = datasetRepository.findById(buyRequestDTO.getDatasetId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND));

        User consumer = userService.findUserById(tokenService.getUserIdFromRequest(request));

        if(buyRequestDTO.getIsHaveSub()){
            return createSubPayment(dataset, consumer);
        } else {
            DatasetPricing datasetPricing = datasetPricingRepository.findById(buyRequestDTO.getDatasetPricingId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_PRICING_NOT_FOUND));
            return createOneTimePayment(dataset, datasetPricing, consumer);
        }
    }

    @Override
    public ConsumerBuyResponseDTO subRegister(long pricingSubRuleId, HttpServletRequest request) {
        User consumer = userService.findUserById(tokenService.getUserIdFromRequest(request));
        PricingRule pricingRule = priceService.findSubPricingRuleById(pricingSubRuleId);

        if(consumerSubRepo.existsByPricingRuleAndConsumerAndIsActive(pricingRule, consumer, true)){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.EXISTS_SUB);
        }

        paymentService.updateWallet(TransferType.PAYOUT, Double.parseDouble(String.valueOf(pricingRule.getBasePricePoint())), tokenService.getUserIdFromRequest(request), BuyType.BUY_SUB);

        List<ConsumerSubscription> consumerSubscriptionList = consumerSubRepo.findAllByConsumerAndIsUsing(consumer, true);
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

        return datasetMapper.toConsumerBuyResponseDTO(PricingMethod.SUBSCRIPTION, consumerSubRepo.save(consumerSubscription));
    }

    @Override
    public List<ConsumerSubscription> findConsumerSub(User consumer) {
        return consumerSubRepo.findByConsumer(consumer);
    }

    @Override
    public ConsumerBuyResponseDTO selectSubPack(long consumerSubId, HttpServletRequest request) {
        User consumer = userService.findUserById(tokenService.getUserIdFromRequest(request));
        ConsumerSubscription consumerSubscription = consumerSubRepo.findById(consumerSubId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.SUB_NOT_FOUND));

        consumerSubscription.setUsing(true);
        List<ConsumerSubscription> consumerSubscriptions = consumerSubRepo.findAllByConsumerAndIsUsing(consumer, true);
        for(ConsumerSubscription sub : consumerSubscriptions){
            sub.setUsing(false);
        }
        consumerSubRepo.saveAll(consumerSubscriptions);
        return datasetMapper.toConsumerBuyResponseDTO(PricingMethod.SUBSCRIPTION, consumerSubRepo.save(consumerSubscription));
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
        return timeGroupRepository.findById(datasetTimeGroupId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND))
                .getDatasets()
                .stream()
                .map(datasetMapper::toDatasetDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConsumerBuyResponseDTO buyWithTimeGroup(long timeGroupId, HttpServletRequest request) {
        TimeGroup timeGroup = timeGroupRepository.findById(timeGroupId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.TIME_GROUP_NOT_FOUND));

        double price = timeGroup.getPrice();
        User user = userService.findUserById(tokenService.getUserIdFromRequest(request));
        paymentService.updateWallet(TransferType.PAYOUT, price, user.getId(), BuyType.BUY_WITH_TIME_GROUP);
        DownloadToken downloadToken = jwtUtil.generateDowloadToken(user, null, 30, 5, timeGroup);

        return datasetMapper.toConsumerBuyResponseDTO(PricingMethod.BUY_WITH_TIME_GROUP, downloadToken);
    }

    @Override
    public void saveTimeGroup(TimeGroup timeGroup) {
        timeGroupRepository.save(timeGroup);
    }

    private ConsumerBuyResponseDTO createSubPayment(Dataset dataset, User consumer) {
        ConsumerSubscription consumerSubscription = consumerSubRepo.findByConsumerAndIsUsing(consumer, true)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.SUB_NOT_FOUND));

        long datasetRow = dataset.getRowCount();

        if(consumerSubscription.getRow_amount() < datasetRow){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.SUB_ROW_NOT_ENOUGH);
        }

        if (consumerSubscription.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.SUB_EXPIRED);
        }

        buyWithSubProcess(consumerSubscription, datasetRow);

        DownloadToken downloadToken = jwtUtil.generateDowloadToken(consumer, dataset, 30, 10, null);
        consumer.getDownloadTokens().add(downloadToken);
        userService.saveUser(consumer);

        OrderRequest orderReq = new OrderRequest();
        orderReq.setDatasetId(dataset.getId());
        orderReq.setDatasetName(dataset.getName());
        orderReq.setPrice(datasetRow);
        orderReq.setPricingMethod(PricingMethod.SUBSCRIPTION);

        ConsumerOrderResponse order = orderService.createOrder(consumer.getId(), List.of(orderReq));

        ConsumerBuyResponseDTO dto = datasetMapper.toConsumerBuyResponseDTO(PricingMethod.SUBSCRIPTION, consumerSubscription);
        dto.getBuySubInfoDTO().setDownloadToken(downloadToken.getId().toString());
        dto.setOrderId(order.getId());

        return dto;
    }

    private ConsumerBuyResponseDTO createOneTimePayment(Dataset dataset, DatasetPricing pricing, User consumer) {
        if (downloadTokenRepository.findByConsumerAndDatasetAndIsActive(consumer, dataset, true).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.DATASET_BOUGHT);
        }

        DownloadToken token = jwtUtil.generateDowloadToken(consumer, dataset, 30, 2, null);
        consumer.getDownloadTokens().add(token);
        userService.saveUser(consumer);

        paymentService.updateWallet(TransferType.PAYOUT, pricing.getPrice(), consumer.getId(), BuyType.BUY_ONE_TIME_DATASET);

        Wallet wallet = walletService.findWalletByUserId(consumer.getId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.WALLET_NOT_FOUND));

        transactionService.createTransaction(TransferType.PAYOUT, pricing.getPrice(), consumer.getId(), wallet, BuyType.BUY_ONE_TIME_DATASET);

        OrderRequest item = new OrderRequest();
        item.setDatasetId(dataset.getId());
        item.setDatasetName(dataset.getName());
        item.setPrice(pricing.getPrice());
        item.setPricingMethod(PricingMethod.ONE_TIME);

        ConsumerOrderResponse order = orderService.createOrder(consumer.getId(), List.of(item));

        ConsumerBuyResponseDTO dto = datasetMapper.toConsumerBuyResponseDTO(PricingMethod.ONE_TIME, token.getId());
        dto.setOrderId(order.getId());

        return dto;
    }

    @Override
    public Dataset uploadCSVFileToPendingFolder(MultipartFile file, Dataset dataset) {

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new RuntimeException("Invalid file name");
        }

        String fileKey = "PENDING/" + UUID.randomUUID() + "_" + originalName;

        try {
            Path base = Paths.get(UPLOAD_BASE).toAbsolutePath().normalize();
            Path pendingDir = base.resolve("PENDING");
            Files.createDirectories(pendingDir);

            Path destination = pendingDir.resolve(fileKey.substring(fileKey.indexOf("/") + 1));

            logger.warn("Saving file to: {}", destination.toAbsolutePath());

            // THE CORRECT AND ONLY WAY
            file.transferTo(destination.toFile());

            if (!Files.exists(destination) || Files.size(destination) == 0) {
                throw new RuntimeException("File not saved properly");
            }

            logger.info("File saved SUCCESSFULLY at {}", destination.toAbsolutePath());

            dataset.setFileKey(fileKey);
            dataset.setFileUrl(destination.toAbsolutePath().toString());
            dataset.setName(originalName);
            dataset.setDatasetStatus(DatasetStatus.PENDING);

            return dataset;

        } catch (Exception e) {
            logger.error("UPLOAD ERROR: {}", e.getMessage(), e);
            throw new RuntimeException("UPLOAD ERROR", e);
        }
    }

    @Override
    public Dataset uploadCSVFileToSytemFolder(MultipartFile file, Dataset dataset) {
        String originalFileName = file.getOriginalFilename();
        String fileKey = "SYSTEM/" + UUID.randomUUID() + "_" + originalFileName;

        try {
            // Base folder (ví dụ: /uploads)
            Path base = Paths.get(UPLOAD_BASE).toAbsolutePath().normalize();
            Path systemDir = base.resolve("SYSTEM");

            logger.info("[UPLOAD] Uploading to SYSTEM: {}", originalFileName);

            // Tạo folder nếu chưa có
            Files.createDirectories(systemDir);

            // Tên file đích
            String destFileName = fileKey.substring(fileKey.indexOf("/") + 1);
            Path destination = systemDir.resolve(destFileName);

            // Copy stream từ MultipartFile sang file hệ thống
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            // Kiểm tra tồn tại
            File destFile = destination.toFile();
            if (!destFile.exists() || destFile.length() == 0) {
                throw new IOException("File not copied correctly!");
            }

            logger.info("[UPLOAD] File uploaded: {} ({} bytes)",
                    destination.toAbsolutePath(), destFile.length());

            // Cập nhật dataset
            dataset.setFileKey(fileKey);
            dataset.setFileUrl(destination.toAbsolutePath().toString());
            dataset.setName(originalFileName);
            dataset.setDatasetStatus(DatasetStatus.APPROVE);

            return dataset;

        } catch (Exception e) {
            logger.error("[UPLOAD] Error uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Error uploading multipart file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Dataset moveFileFromPendingToApproveFolder(Dataset dataset) {
        String oldFileUrl = dataset.getFileUrl();
        String oldFileKey = dataset.getFileKey();

        if (oldFileUrl == null || !oldFileKey.startsWith("PENDING/")) {
            throw new IllegalArgumentException("Dataset file is not in pending folder");
        }

        try {
            Path oldPath = Paths.get(oldFileUrl);

            if (!Files.exists(oldPath)) {
                throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.FILE_NOT_FOUND);
            }

            Path base = Paths.get(UPLOAD_BASE).toAbsolutePath().normalize();
            Path approvedDir = base.resolve("APPROVED");
            Files.createDirectories(approvedDir);

            String fileName = oldPath.getFileName().toString();
            String newFileKey = "APPROVED/" + UUID.randomUUID() + "_" + fileName;
            Path newPath = approvedDir.resolve(newFileKey.substring(newFileKey.indexOf("/") + 1));

            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("[MOVE] File moved from PENDING to APPROVED: {}", newPath.toAbsolutePath());

            dataset.setFileKey(newFileKey);
            dataset.setFileUrl(newPath.toAbsolutePath().toString());
            dataset.setDatasetStatus(DatasetStatus.APPROVE);

            return datasetRepository.save(dataset);

        } catch (IOException e) {
            logger.error("[MOVE] Error moving file: {}", e.getMessage(), e);
            throw new RuntimeException("Error moving file from pending to approved: " + e.getMessage(), e);
        }
    }

    @Override
    public String getDownloadTokenOfDatasetForConsumer(long datasetId, HttpServletRequest request) {
        User user = userService.findUserById(tokenService.getUserIdFromRequest(request));
        Dataset dataset = findById(datasetId);
        Optional<DownloadToken> downloadTokenOptional = downloadTokenRepository.findByConsumerAndDatasetAndIsActive(user, dataset, true);

        if(downloadTokenOptional.isPresent()){
            if(downloadTokenOptional.get().getUse_amount() <= 0 || downloadTokenOptional.get().getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.TOKEN_IS_EXPIRED);
            }
        }
        if(downloadTokenOptional.isEmpty()){
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.TOKEN_NOT_FOUND);
        }
        return downloadTokenOptional.get().getId().toString();
    }

    private Dataset findById(long datasetId) {
        return datasetRepository.findById(datasetId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND));
    }

    @Override
    public ResponseEntity<?> getAllAllDataset() {
        List<DatasetParentReposonseDto> datasetReposonseDtoList = datasetGroupRepository.findAll()
                .stream()
                .map(datasetMapper::toDatasetParentReposonseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(new ApiResponse(true, "load dataset success", datasetReposonseDtoList));
    }

    @Override
    public ConsumerBuyResponseDTO buyTimeGroupWithSub(long timeGroupId, HttpServletRequest request) {
        TimeGroup timeGroup = timeGroupRepository.findById(timeGroupId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.TIME_GROUP_NOT_FOUND));

        User consumer = userService.findUserById(tokenService.getUserIdFromRequest(request));

        ConsumerSubscription consumerSubscription = consumerSubRepo.findByConsumerAndIsUsing(consumer, true)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.SUB_NOT_FOUND));

        long timeGroupRowCount = timeGroup.getRow_Count();

        buyWithSubProcess(consumerSubscription, timeGroupRowCount);

        DownloadToken downloadToken = jwtUtil.generateDowloadToken(consumer, null, 30, 10, timeGroup);

        return datasetMapper.toConsumerBuyResponseDTO(PricingMethod.BUY_WITH_TIME_GROUP, downloadToken);
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

        paymentService.updateWallet(TransferType.PAYOUT, price, user.getId(), BuyType.BUY_API);

        DownloadToken downloadToken = jwtUtil.generateDowloadToken(user, null, 30, 5, null);

        return datasetMapper.toConsumerBuyResponseDTO(PricingMethod.API, downloadToken);
    }

    public void buyWithSubProcess(ConsumerSubscription consumerSubscription, long rowCount){
        long rowCountConsumer = consumerSubscription.getRow_amount();

        if(rowCountConsumer < rowCount){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.SUB_ROW_NOT_ENOUGH);
        }
        consumerSubscription.setRow_amount(rowCountConsumer - rowCount);
        consumerSubRepo.save(consumerSubscription);
    }


    @Override
    public ResponseEntity<?> downloadDataset(String dowloadToken, HttpServletRequest request, DowloadType dowloadType) {

        Optional<DownloadToken> downloadTokenOptional = downloadTokenRepository.findById(UUID.fromString(dowloadToken));
        User user = userService.findUserById(tokenService.getUserIdFromRequest(request));

        if (downloadTokenOptional.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.TOKEN_NOT_FOUND);
        }
        if (downloadTokenOptional.get().getConsumer() != user) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }
        if (downloadTokenOptional.get().getUse_amount() == 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.TOKEN_IS_EXPIRED);
        }

        DownloadToken downloadToken = downloadTokenOptional.get();
        Dataset dataset = downloadToken.getDataset();
        String filePathStr = dataset.getFileUrl();

        if (filePathStr == null || filePathStr.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.FILE_NOT_FOUND);
        }

        Path filePath = Paths.get(filePathStr);
        if (!Files.exists(filePath)) {
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.FILE_NOT_FOUND);
        }

        try {
            // nếu user yêu cầu JSON -> chuyển CSV thành JSON
            if (dowloadType == DowloadType.JSON) {
                String lower = filePathStr.toLowerCase();
                if (!lower.endsWith(".csv")) {
                    // Bạn có thể đổi ErrorCode này thành mã hợp lý trong project của bạn
                    throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FILE_NOT_FOUND);
                }

                File csvFile = filePath.toFile();
                List<Map<String, Object>> jsonList = fileService.csvToJson(csvFile); // method bạn đã viết

                // chuyển List thành JSON string (dùng Jackson)
                ObjectMapper mapper = new ObjectMapper();
                // nếu muốn pretty print:
                // mapper.enable(SerializationFeature.INDENT_OUTPUT);
                byte[] jsonBytes = mapper.writeValueAsBytes(jsonList);

                // cập nhật token & dataset giống như trước
                downloadToken.setUse_amount(downloadToken.getUse_amount() - 1);
                if (downloadToken.getUse_amount() == 0) {
                    downloadToken.setActive(false);
                }
                downloadTokenRepository.save(downloadToken);

                dataset.setDownloadCount(dataset.getDownloadCount() + 1);
                datasetRepository.save(dataset);

                // trả về JSON như một file đính kèm với tên đổi thành .json
                String jsonFileName = filePath.getFileName().toString().replaceAll("\\.csv$", ".json");
                InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(jsonBytes));

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + jsonFileName + "\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentLength(jsonBytes.length)
                        .body(resource);
            }

            // Nếu không phải JSON, trả về file gốc (nhị phân) như cũ
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));

            downloadToken.setUse_amount(downloadToken.getUse_amount() - 1);
            if (downloadToken.getUse_amount() == 0) {
                downloadToken.setActive(false);
            }
            downloadTokenRepository.save(downloadToken);

            dataset.setDownloadCount(dataset.getDownloadCount() + 1);
            datasetRepository.save(dataset);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(Files.size(filePath))
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException("Error reading file for download: " + filePathStr, e);
        }
    }

    @Override
    public ResponseEntity<?> downloadDatasetNoValidToken(Long datasetId) {

        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND));

        String fileUrl = dataset.getFileUrl();
        System.out.println(fileUrl);
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.FILE_NOT_FOUND);
        }

        try {
            Path filePath = Paths.get(fileUrl);

            if (!Files.exists(filePath)) {
                throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.FILE_NOT_FOUND);
            }

            InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));

            dataset.setDownloadCount(dataset.getDownloadCount() + 1);
            datasetRepository.save(dataset);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(Files.size(filePath))
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException("Error reading file from local storage: " + fileUrl, e);
        }
    }

    @Override
    public List<DatasetDTO> findAllConsumerDataset(HttpServletRequest request) {
        User user = userService.findUserById(tokenService.getUserIdFromRequest(request));
        return downloadTokenRepository.findByConsumerAndIsActive(user, true).stream()
                .map(DownloadToken::getDataset)
                .map(datasetMapper::toDatasetDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConsumerBuyResponseDTO buyDatasetWithSub(long datasetId, HttpServletRequest request) {
        User user = userService.findUserById(tokenService.getUserIdFromRequest(request));

        ConsumerSubscription consumerSubscription = consumerSubRepo.findByConsumerAndIsUsing(user, true)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.SUB_NOT_FOUND));
        return null;
    }

    @Override
    public void moderatorCreateNewDatasetGroup(ModeratorCreateNewDatasetGroupRequest moderatorCreateNewDatasetGroupRequest) {
        DatasetGroup parentDatasetGroup = new DatasetGroup();
        parentDatasetGroup.setDatasetType(datasetTypeRepository.findById(moderatorCreateNewDatasetGroupRequest.getDataset_type_id())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.INVALID_TYPE_ID)));

        DatasetGroup childDatasetGroup = new DatasetGroup();
        childDatasetGroup.setParent(parentDatasetGroup);
        parentDatasetGroup.setDatasetSourceType(DatasetSourceType.SYSTEM_DATASET);
        childDatasetGroup.setDatasetSourceType(DatasetSourceType.SYSTEM_DATASET);

        datasetGroupRepository.save(childDatasetGroup);
        datasetGroupRepository.save(parentDatasetGroup);
    }

    @Override
    public List<DatasetParentReposonseDto> findAllSystamDatasetGroup() {
        return datasetGroupRepository.findByDatasetGroupTypeAndDatasetSourceType(DatasetGroupType.PARENT, DatasetSourceType.SYSTEM_DATASET)
                .stream()
                .map(datasetMapper::toDatasetParentReposonseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DatasetParentReposonseDto> findAllProviderDataset() {
        return datasetGroupRepository.findAllByDatasetGroupTypeAndDatasetSourceType(DatasetGroupType.PARENT, DatasetSourceType.DATASET_PROVIDER)
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
    public List<DatasetDTO> getAllProviderDataset(HttpServletRequest request) {
        Provider provider = userService.findProviderById(tokenService.getUserIdFromRequest(request));
        return datasetRepository.findByProvider(provider)
                .stream()
                .map(datasetMapper::toDatasetDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DatasetDTO getDatasetDetail(Long id, HttpServletRequest request) {
        Provider provider = userService.findProviderById(tokenService.getUserIdFromRequest(request));
        Dataset dataset = datasetRepository.findByIdAndProvider(id, provider);
        return datasetMapper.toDatasetDTO(dataset);
    }

    @Override
    public boolean cancelDataset(Long id, HttpServletRequest request) {
        Dataset dataset = datasetRepository.findByIdAndProvider(id,
                userService.findProviderById(tokenService.getUserIdFromRequest(request)));

        if(!(dataset.getProvider().equals(userService.findProviderById(tokenService.getUserIdFromRequest(request))))){
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.CAN_NOT_CANCEL_DATASET);
        }

        if(!(dataset.getDatasetStatus() == DatasetStatus.PENDING)){
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.CAN_NOT_CANCEL_DATASET);
        }

        dataset.setDatasetStatus(DatasetStatus.CANCEL);
        datasetRepository.save(dataset);
        return true;
    }


    @Override
    public DatasetUpdateResponse updateDataset(Long id, DatasetUpdateRequest datasetUpdateRequest) {
        String token = tokenService.resolveToken(request);
        if(token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if(userId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.USER_NOT_FOUND);
        }
        User moderator = userService.findUserById(userId);
        if(!moderator.getRole().getName().equalsIgnoreCase("MODERATOR")){
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }

        if(datasetUpdateRequest.getDatasetName() == null || datasetUpdateRequest.getDatasetName().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
        }

        if(datasetUpdateRequest.getTitle() == null || datasetUpdateRequest.getTitle().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
        }

        if(datasetUpdateRequest.getDescription() == null || datasetUpdateRequest.getDescription().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
        }

        if(datasetUpdateRequest.getStatus() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
        }

        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND));

        DatasetPlan plan = datasetPlanRepo.findByDatasetAndPricingMethod(dataset, PricingMethod.ONE_TIME);

        DatasetPricing price = datasetPricingRepository.findDatasetPricingByDatasetPlanAndPricingMethod(plan,PricingMethod.ONE_TIME);

        price.setPrice(datasetUpdateRequest.getPrice());
        datasetPricingRepository.save(price);

        dataset.setName(datasetUpdateRequest.getDatasetName());
        dataset.setDescription(datasetUpdateRequest.getDescription());
        dataset.setTitle(datasetUpdateRequest.getTitle());
        dataset.setDatasetStatus(datasetUpdateRequest.getStatus());

        Dataset saved = datasetRepository.save(dataset);

        return new DatasetUpdateResponse(
                saved.getId(),
                saved.getName(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getDatasetStatus().name(),
                price.getPrice()
        );
    }

    @Override
    public List<ListUpdateDatasetResponse> getAll() {
        String token = tokenService.resolveToken(request);
        if(token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if(userId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.USER_NOT_FOUND);
        }
        User moderator = userService.findUserById(userId);
        if(!moderator.getRole().getName().equalsIgnoreCase("MODERATOR")){
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }
        List<Dataset> dataset = datasetRepository.findAll();
        List<ListUpdateDatasetResponse> datasetResponseList = dataset.stream()
                .map(l ->
                        new ListUpdateDatasetResponse(
                                l.getId(),
                                l.getName(),
                                l.getDatasetStatus()
                        )).toList();
        return datasetResponseList;
    }

    @Override
    public DatasetUpdateResponse getDetailDataset(Long id) {
        String token = tokenService.resolveToken(request);
        if(token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if(userId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.USER_NOT_FOUND);
        }
        User moderator = userService.findUserById(userId);
        if(!moderator.getRole().getName().equalsIgnoreCase("MODERATOR")){
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND));

        DatasetPlan plan = datasetPlanRepo.findByDatasetAndPricingMethod(dataset, PricingMethod.ONE_TIME);

        DatasetPricing pricing = datasetPricingRepository.findDatasetPricingByDatasetPlanAndPricingMethod(plan,PricingMethod.ONE_TIME);

        Double price = pricing.getPrice();

        return new DatasetUpdateResponse(
                dataset.getId(),
                dataset.getName(),
                dataset.getTitle(),
                dataset.getDescription(),
                dataset.getDatasetStatus().name(),
                price
        );
    }

    @Override
    public ConsumerBuyResponseDTO buyAPIPack(ConsumerBuyRequestDTO buyRequestDTO, HttpServletRequest request) {
        DatasetPricing datasetPricing = datasetPricingRepository.findById(buyRequestDTO.getDatasetPricingId()).orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND,ErrorCode.API_PACK_NOT_FOUND));
        Dataset dataset = datasetRepository.findById(buyRequestDTO.getDatasetId()).orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND,ErrorCode.DATASET_NOT_FOUND));
        double price = 0.0;
        String apiToken="";
        if(buyRequestDTO.getSubType()==null) {
            System.out.println(buyRequestDTO.getDatasetPricingId());
            price = datasetPricing.getPrice();

            paymentService.updateWallet(TransferType.PAYOUT, price, tokenService.getUserIdFromRequest(request), BuyType.BUY_API);

            apiToken = jwtUtil.generateApiSaleToken(userService.findUserById(tokenService.getUserIdFromRequest(request)), dataset, 31L, datasetPricing.getPricingRule().getRequestLimit());
        }
        else {
            if (buyRequestDTO.getSubType().equalsIgnoreCase("LARGE")) {
                if (!consumerSubRepo.existsByConsumerAndIsActiveAndIsUsing(userService.findUserById(tokenService.getUserIdFromRequest(request)), true, true)) {
                    throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.CONSUMER_SUB_NOT_FOUND);
                }
                 apiToken = jwtUtil.generateApiSaleToken(userService.findUserById(tokenService.getUserIdFromRequest(request)), dataset, 31L, datasetPricing.getPricingRule().getRequestLimit());
            }
        }
        List<OrderRequest> orderRequests = new ArrayList<>();
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setDatasetId(dataset.getId());
        orderRequest.setDatasetName(dataset.getName());
        orderRequest.setPrice(price);
        orderRequest.setPricingMethod(PricingMethod.API);
        orderRequests.add(orderRequest);

        orderService.createOrder(tokenService.getUserIdFromRequest(request),orderRequests);
        return datasetMapper.toConsumerBuyResponseDTO(PricingMethod.API, apiToken);
    }

    @Override
    public ResponseEntity<?> getDataForApiBuying(String token) {
        try {
            // 1. Kiểm tra token hợp lệ + chưa hết hạn + chưa bị revoke + chưa hết lượt
            if (!jwtUtil.consumeApiToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "success", false,
                                "message", "Token không hợp lệ hoặc đã hết hạn / hết lượt sử dụng"
                        ));
            }

            // 2. Lấy dataset từ token
            Dataset dataset = jwtUtil.finđDatasetFromAPIToken(token);
            if (dataset == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "message", "Không tìm thấy dataset trong token"
                        ));
            }

            // 3. Lấy đường dẫn file từ dataset (tùy model của bạn)
            File file = fileService.getFileFromDataset(dataset);
            if (file == null || !file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "File dữ liệu không tồn tại"
                        ));
            }

            // 4. Convert CSV → JSON
            Object jsonResult = fileService.csvToJson(file);

            // 5. Trả kết quả cho client
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "datasetId", dataset.getId(),
                            "data", jsonResult
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Lỗi xử lý API: " + e.getMessage()
                    ));
        }
    }

    @Override
    public List<ApiTokenResponse> findAllTokenForConsumer() {
        return tokenService.findAllDownloadTokenForConsumer(userService.findUserById(tokenService.getUserIdFromRequest(request))).stream().map(datasetMapper::toApiTokenResponse).collect(Collectors.toList());
    }


    @Override
    public List<Dataset> findAllByStatus(DatasetStatus datasetStatus) {
        return datasetRepository.findAllByDatasetStatus(datasetStatus);
    }
}