package com.example.datasetapi.config.system;

import com.example.datasetapi.enums.Datasets.*;
import com.example.datasetapi.enums.DocumentType;
import com.example.datasetapi.enums.UserStatus;
import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.enums.VerificationStatus.VerificationStatus;
import com.example.datasetapi.enums.Datasets.FileExtension;
import com.example.datasetapi.model.dataset.*;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import com.example.datasetapi.model.paySystem.BankAccount;
import com.example.datasetapi.model.userManager.*;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.repository.*;
import com.example.datasetapi.service.payment.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.ConsumerTypeRepository;
import com.example.datasetapi.repository.RoleRepository;
import com.example.datasetapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConsumerTypeRepository consumerTypeRepository;
    @Autowired
    private ProviderRegistrationRepository providerRegistrationRepository;
    @Autowired
    private ProviderIndentityDocumentRepository providerIndentityDocumentRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private DatasetTypeRepository datasetTypeRepository;
    @Autowired
    private Dataset_Type_Column_Repository datasetTypeColumnRepository;
    @Autowired
    private DatasetRepository datasetRepository;
    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private PricingRuleRepo pricingRuleRepo;
    @Autowired
    private ProvinceRepository provinceRepository;
    @Autowired
    private CommuneRepository communeRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            roleRepository.save(new Role("ADMIN"));
            roleRepository.save(new Role("PROVIDER"));
            roleRepository.save(new Role("CONSUMER"));
            roleRepository.save(new Role("MODERATOR"));
            System.out.println("Roles & permissions initialized.");
            initVietnamLocations();
            createConsumerRole();
            createAdminRole();
            createConsumerTypes();
            createModerator();
            System.out.println("Seeded roles, users, and consumer types.");

            // tao va gan provider dang ky mau tranh trung lap thong tin khi create-drop db
            createProviderRegistration();

            createCategory();
            createDatasetType();
            createDataset_Type_Columns();
            assignColumnAndCategoryToDatasetType();

            // createDatasetDemo();
            // createModerationTestData();
            createPricingRule();

        } else {
            System.out.println("Data already Init............" +
                    "======================================Stop Init Data========================================\n");
        }
        System.out.println("===========================Load Data success===========================\n");
    }

    private void createPricingRule() {
        List<PricingRule> rules = new ArrayList<>();

        // ONE TIME - Small
        PricingRule r1 = new PricingRule();
        r1.setMethod(PricingMethod.ONE_TIME);
        r1.setPlanName("Small Pack");
        r1.setMinRow(1000L);
        r1.setMaxRow(50000L);
        // giá tính theo 1 row
        r1.setBasePricePerRowPoint(15.0);
        r1.setAllowOverage(false);
        r1.setProviderShare(40);
        r1.setPlatformShare(60);
        r1.setDatasetPack(DatasetPack.SMALL);
        r1.setNote("Gói nhỏ cho nhu cầu thấp");
        rules.add(r1);

        // ONE TIME - Medium
        PricingRule r2 = new PricingRule();
        r2.setMethod(PricingMethod.ONE_TIME);
        r2.setPlanName("Medium Pack");
        r2.setMinRow(50001L);
        r2.setMaxRow(200000L);
        // giá tính theo row
        r2.setBasePricePerRowPoint(1.5);
        r2.setDiscountPercent(5);
        r2.setAllowOverage(false);
        r2.setProviderShare(35);
        r2.setPlatformShare(65);
        r2.setDatasetPack(DatasetPack.MEDIUM);
        r2.setNote("Gói trung bình cho nhu cầu vừa");
        rules.add(r2);

        // ONE TIME - Large
        PricingRule r3 = new PricingRule();
        r3.setMethod(PricingMethod.ONE_TIME);
        r3.setPlanName("Large Pack");
        r3.setMinRow(200001L);
        r3.setMaxRow(600000L);
        // giá tính theo row
        r3.setBasePricePerRowPoint(1.5);
        r3.setDiscountPercent(10);
        r3.setAllowOverage(false);
        r3.setProviderShare(30);
        r3.setPlatformShare(70);
        r3.setDatasetPack(DatasetPack.LARGE);
        r3.setNote("Gói lớn cho nhu cầu cao");
        rules.add(r3);

        // SUBSCRIPTION - Free tier
        PricingRule r4 = new PricingRule();
        r4.setMethod(PricingMethod.SUBSCRIPTION);
        r4.setPlanName("Free tier");
        r4.setBasePricePoint(0.0);
        r4.setRowLimit(50000L);
        r4.setTimeLimitDay(30);
        r4.setExtraCostPer1rowpoint(4.0);
        r4.setAllowOverage(true);
        r4.setProviderShare(40);
        r4.setPlatformShare(60);
        r4.setSubType(SubType.SMALL);
        r4.setNote("Gói cơ bản");
        rules.add(r4);

        // SUBSCRIPTION - Basic Monthly
        PricingRule r5 = new PricingRule();
        r5.setMethod(PricingMethod.SUBSCRIPTION);
        r5.setPlanName("Basic Monthly");
        r5.setBasePricePoint(1500000.0);
        r5.setRowLimit(50000L);
        r5.setTimeLimitDay(30);
        r5.setExtraCostPer1rowpoint(4.0);
        r5.setAllowOverage(true);
        r5.setProviderShare(40);
        r5.setPlatformShare(60);
        r5.setSubType(SubType.MEDIUM);
        r5.setNote("Gói thuê bao 1 tháng \n Thoải mái download dataset không giới hạn");
        rules.add(r5);

        // SUBSCRIPTION - Premium
        PricingRule r6 = new PricingRule();
        r6.setMethod(PricingMethod.SUBSCRIPTION);
        r6.setPlanName("Premium");
        r6.setBasePricePoint(2500000.0);
        r6.setRowLimit(200000L);
        r6.setTimeLimitDay(60);
        r6.setExtraCostPer1rowpoint(3.0);
        r6.setDiscountPercent(5);
        r6.setAllowOverage(true);
        r6.setProviderShare(35);
        r6.setPlatformShare(65);
        r6.setSubType(SubType.LARGE);
        r6.setNote("Gói thuê bao 3 tháng \nThoải mái tải dataset\n Có dashboard thể hiện thông tin được tổng hợp bằng AI ");
        rules.add(r6);


// Gói nhỏ (bạn đã có)
        PricingRule r8 = new PricingRule();
        r8.setMethod(PricingMethod.PROVIDER_REVENUE);
        r8.setPlanName("Provider Revenue Small Pack");
        r8.setBasePricePoint(2500000.0);
        r8.setDatasetPack(DatasetPack.SMALL);
        r8.setNote("Lợi Nhuận cho provider với gói nhỏ");
        rules.add(r8);

// Gói vừa
        PricingRule r9 = new PricingRule();
        r9.setMethod(PricingMethod.PROVIDER_REVENUE);
        r9.setPlanName("Provider Revenue Medium Pack");
        r9.setBasePricePoint(5000000.0); // giá gợi ý
        r9.setDatasetPack(DatasetPack.MEDIUM);
        r9.setNote("Lợi Nhuận cho provider với gói vừa");
        rules.add(r9);

// Gói lớn
        PricingRule r10 = new PricingRule();
        r10.setMethod(PricingMethod.PROVIDER_REVENUE);
        r10.setPlanName("Provider Revenue Large Pack");
        r10.setBasePricePoint(10000000.0); // giá gợi ý
        r10.setDatasetPack(DatasetPack.LARGE);
        r10.setNote("Lợi Nhuận cho provider với gói lớn");
        rules.add(r10);

        PricingRule r11 = new PricingRule();
        r11.setMethod(PricingMethod.API);
        r11.setPlanName("Consumer API small pack");
        r11.setDatasetPack(DatasetPack.SMALL);
        r11.setBasePricePoint(2500000.0);
        r11.setNote("Lợi Nhuận cho provider với gói lớn");
        r11.setRequestLimit(100l);
        rules.add(r11);

        PricingRule r12 = new PricingRule();
        r12.setMethod(PricingMethod.API);
        r12.setPlanName("Consumer API Medium Pack");
        r12.setBasePricePoint(2500000.0);
        r12.setDatasetPack(DatasetPack.MEDIUM);
        r12.setNote("Lợi Nhuận cho provider với gói lớn");
        r12.setRequestLimit(100L);
        rules.add(r12);

        PricingRule r13 = new PricingRule();
        r13.setMethod(PricingMethod.API);
        r13.setPlanName("Consumer API Large Pack");
        r13.setBasePricePoint(2500000.0);
        r13.setDatasetPack(DatasetPack.LARGE);
        r13.setNote("Lợi Nhuận cho provider với gói lớn");
        r13.setRequestLimit(100L);
        rules.add(r13);


        pricingRuleRepo.saveAll(rules);
    }

    private void createModerator() {
        User user = new User();
        user.setUsername("moderator");
        user.setActive(true);
        user.setPassword(passwordEncoder.encode("moderator"));
        user.setRole(roleRepository.findByName("MODERATOR").get());
        user.setEmail("moderator@gmail.com");
        user.setIsEmailValid(true);
        userRepository.save(user);
    }

    // private void createModerationTestData() { ... } // giữ nguyên comment nếu cần

    private void createDatasetDemo() {
        DatasetGroup datasetGroup = new DatasetGroup();
        datasetGroup.setDatasetType(datasetTypeRepository.getOne(1L));

        Dataset dataset = new Dataset();
        dataset.setFileKey("testUpload.txt");
        dataset.setName("testDataset");
        dataset.setFileKey("testUpload.txt");
        dataset.setDescription("testDatasetDescription");
        dataset.setDatasetChildGroup(datasetGroup);
        datasetRepository.save(dataset);
    }


    private void assignColumnAndCategoryToDatasetType() {
        // Load toàn bộ cột và đưa về lowercase để dễ so khớp
        List<DatasetTypeColumn> allCols = datasetTypeColumnRepository.findAll();
        Map<String, DatasetTypeColumn> colMap = allCols.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        c -> normalizeColumnName(c.getColumnName()),
                        c -> c,
                        (existing, replacement) -> existing
                ));

        java.util.function.Function<List<String>, List<DatasetTypeColumn>> resolveCols = (rawNames) -> {
            List<DatasetTypeColumn> result = new ArrayList<>();
            for (String n : rawNames) {
                String key = normalizeColumnName(n);
                if (colMap.containsKey(key)) {
                    result.add(colMap.get(key));
                } else {
                    System.out.println("⚠️ Column not found: " + n);
                }
            }
            return result;
        };

        // === STATION_ENERGY ===
        DatasetType stationEnergy = datasetTypeRepository.findByName("STATION_ENERGY");
        if (stationEnergy != null) {
            List<String> needed = Arrays.asList(
                    "session_id", "station_id", "start_time", "end_time", "duration_min",
                    "charging_mode", "voltage_avg", "current_avg", "power_peak_kw",
                    "energy_kwh", "charging_efficiency", "temperature_max", "power_factor",
                    "created_at", "updated_at"
            );
            List<DatasetTypeColumn> cols = resolveCols.apply(needed);
            stationEnergy.setDatasetTypeColumnList(cols);
            datasetTypeRepository.save(stationEnergy);
            System.out.println("Assigned " + cols.size() + " columns to STATION_ENERGY");
        }

        // === TRANSACTION_BILLING ===
        DatasetType transactionBilling = datasetTypeRepository.findByName("TRANSACTION_BILLING");
        if (transactionBilling != null) {
            List<String> needed = Arrays.asList(
                    "transaction_id", "session_id", "pricing_plan", "unit_price",
                    "energy_kwh_billed", "total_cost", "payment_method", "created_at"
            );
            List<DatasetTypeColumn> cols = resolveCols.apply(needed);
            transactionBilling.setDatasetTypeColumnList(cols);
            datasetTypeRepository.save(transactionBilling);
            System.out.println("Assigned " + cols.size() + " columns to TRANSACTION_BILLING");
        }

        // === VEHICLE_DATA_SAMPLE ===
        DatasetType vehicleSample = datasetTypeRepository.findByName("VEHICLE_DATA_SAMPLE");
        if (vehicleSample != null) {
            List<String> needed = Arrays.asList(
                    "session_id", "vehicle_id", "battery_soc_start", "battery_soc_end",
                    "battery_capacity_kwh", "requested_energy", "vehicle_model"
            );
            List<DatasetTypeColumn> cols = resolveCols.apply(needed);
            vehicleSample.setDatasetTypeColumnList(cols);
            datasetTypeRepository.save(vehicleSample);
            System.out.println("Assigned " + cols.size() + " columns to VEHICLE_DATA_SAMPLE");
        }
    }

    // Helper: normalize về lowercase
    private String normalizeColumnName(String s) {
        if (s == null) return null;
        return s.trim().replaceAll("\\s+", "_").toLowerCase();
    }

    /**
     * ✅ Tạo đúng (và chỉ) các cột thuộc 3 schema bạn yêu cầu
     * - Bỏ toàn bộ cột ngoài 3 block đã nêu
     */
    private void createDataset_Type_Columns() {
        // Union các cột từ 3 schema bạn cung cấp
        List<String> rawColumnNames = Arrays.asList(
                // VEHICLE_DATA_SAMPLE
                "session_id", "vehicle_id", "battery_soc_start", "battery_soc_end",
                "battery_capacity_kwh", "requested_energy", "vehicle_model",

                // TRANSACTION_BILLING
                "transaction_id", "session_id", "pricing_plan", "unit_price",
                "energy_kwh_billed", "total_cost", "payment_method", "created_at",

                // STATION_ENERGY
                "session_id", "station_id", "start_time", "end_time", "duration_min",
                "charging_mode", "voltage_avg", "current_avg", "power_peak_kw", "energy_kwh",
                "charging_efficiency", "temperature_max", "power_factor", "created_at", "updated_at"
        );

        // Chuẩn hoá & loại bỏ trùng (chuyển toàn bộ về chữ thường)
        List<String> normalized = rawColumnNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(s -> s.replaceAll("\\s+", "_").toLowerCase())
                .distinct()
                .toList();

        if (normalized.isEmpty()) {
            System.out.println("No dataset columns to create.");
            return;
        }

        // Lấy tất cả các cột đã có (trong tập normalized)
        List<DatasetTypeColumn> existingCols = datasetTypeColumnRepository.findByColumnNameIn(normalized);
        Set<String> existingNames = existingCols.stream()
                .map(DatasetTypeColumn::getColumnName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Tạo mới nếu chưa tồn tại
        List<DatasetTypeColumn> toCreate = new ArrayList<>();
        for (String colName : normalized) {
            if (!existingNames.contains(colName)) {
                DatasetTypeColumn c = new DatasetTypeColumn();
                c.setColumnName(colName);
                toCreate.add(c);
            }
        }

        if (!toCreate.isEmpty()) {
            datasetTypeColumnRepository.saveAll(toCreate);
            System.out.println("Created " + toCreate.size() + " new dataset type columns (from 3 schemas).");
        } else {
            System.out.println("No new dataset type columns to create (all exist).");
        }
    }

    private void createDatasetType() {
        List<Category> categories;

        // 1. STATION_ENERGY
        DatasetType datasetType1 = new DatasetType();
        datasetType1.setName("STATION_ENERGY");
        categories = categoryRepository.findAllByNameIn(List.of("Location"));
        datasetType1.setCategories(categories);
        datasetTypeRepository.save(datasetType1);

        // 2. TRANSACTION_BILLING
        DatasetType datasetType2 = new DatasetType();
        datasetType2.setName("TRANSACTION_BILLING");
        categories = categoryRepository.findAllByNameIn(List.of("Location", "Operation"));
        datasetType2.setCategories(categories);
        datasetTypeRepository.save(datasetType2);

        // 3. VEHICLE_DATA_SAMPLE
        DatasetType datasetType3 = new DatasetType();
        datasetType3.setName("VEHICLE_DATA_SAMPLE");
        categories = categoryRepository.findAllByNameIn(List.of("Technical", "Operational"));
        datasetType3.setCategories(categories);
        datasetTypeRepository.save(datasetType3);

        System.out.println("khoi tao dataset type");
    }

    private void createCategory() {
        List<String> categories = new ArrayList<>();
        categories.add("Location");
        categories.add("Operation");
        categories.add("Technical");
        categories.add("User Behavior");
        categories.add("Pricing & Payment");
        categories.add("All");

        for (String cate : categories) {
            Category newCategory = new Category();
            newCategory.setName(cate);
            categoryRepository.save(newCategory);
        }
        System.out.println("khoi tao category");
    }

    private void createProviderRegistration() {
        // 1️⃣ Tạo ProviderRegistration
        ProviderRegistration registration = new ProviderRegistration();
        registration.setFullName("Nguyen Van A");
        registration.setEmail("nguyenvana@example.com");
        registration.setPhoneNumber("0123456789");
        registration.setOrganizationName("Cong ty TNHH EV Data");
        registration.setTaxId("0319999999");

        // Gán location (chỉ cần id, vì entity đã có provinceId / communeId)
        registration.setProvinceId("01");   // ví dụ: Hà Nội
        registration.setCommune(communeRepository.findById("00008").get()); // ví dụ: Phường Phúc Xá

        // Trạng thái + thời gian
        registration.setRegistrationStatus(RegistrationStatus.PENDING);

        // 2️⃣ Tạo các giấy tờ định danh (3 loại)
        ProviderIdentityDocument docFront = new ProviderIdentityDocument();
        docFront.setDocumentType(DocumentType.CCCD_FRONT);
        docFront.setImage_url("https://res.cloudinary.com/dofuoy88z/image/upload/v1758869860/seridykcpf1ospeni0zs.jpg");
        docFront.setUploadedAt(Instant.now());
        docFront.setIdCardVerificationStatus(VerificationStatus.PENDING);
        docFront.setManager_id(0L);
        docFront.setIdCardRetentionExpiry(Instant.now().plusSeconds(60L * 60 * 24 * 365));

        ProviderIdentityDocument docBack = new ProviderIdentityDocument();
        docBack.setDocumentType(DocumentType.CCCD_BACK);
        docBack.setImage_url("https://res.cloudinary.com/dofuoy88z/image_upload/v1758869861/oegnzrkytqhvfexxfdrf.jpg".replace("upload/",""));
        docBack.setUploadedAt(Instant.now());
        docBack.setIdCardVerificationStatus(VerificationStatus.PENDING);
        docBack.setManager_id(0L);
        docBack.setIdCardRetentionExpiry(Instant.now().plusSeconds(60L * 60 * 24 * 365));

        ProviderIdentityDocument docOwnership = new ProviderIdentityDocument();
        docOwnership.setDocumentType(DocumentType.OWNER_DOC);
        docOwnership.setImage_url("https://res.cloudinary.com/dofuoy88z/image/upload/v1758869862/oegnzrkytqhvfexxfdrf.png");
        docOwnership.setUploadedAt(Instant.now());
        docOwnership.setIdCardVerificationStatus(VerificationStatus.PENDING);
        docOwnership.setManager_id(0L);
        docOwnership.setIdCardRetentionExpiry(Instant.now().plusSeconds(60L * 60 * 24 * 365));

        // Gắn vào registration
        registration.setIdentityDocuments(List.of(docFront, docBack, docOwnership));

        // 3️⃣ Tạo User cho Provider
        User user = new User();
        user.setUsername("provider");
        user.setEmail("provider@gmail.com");
        user.setIsEmailValid(true);
        user.setPassword(passwordEncoder.encode("password"));
        user.setUserStatus(UserStatus.ACTIVE);
        user.setRole(roleRepository.getRolesByName("PROVIDER"));
        user.setActive(true);

        // 4️⃣ Tạo Provider entity
        Provider provider = new Provider();
        provider.setUser(user);
        provider.setProviderRegistration(registration);

        BankAccount bankAccount = new BankAccount();
        bankAccount.setBankName("Vietcombank");
        bankAccount.setAccountHolderName("Nguyen Van A");
        bankAccount.setAccountNumber("123456789");
        bankAccount.setUser(user);

        // Liên kết location
        Commune commune = communeRepository.findById("00008")
                .orElseThrow(() -> new RuntimeException("Commune not found"));
        Commune commune2 = communeRepository.findById("00118")
                .orElseThrow(() -> new RuntimeException("Commune not found"));
        provider.setCommunes(new ArrayList<>(List.of(commune, commune2)));
        provider.setProviderRegistration(registration);

        // 5️⃣ Lưu xuống database
        providerRepository.save(provider);
        bankAccountRepository.save(bankAccount);
        walletService.createWallet(user);

        System.out.println("✅ ProviderRegistration + Provider + User created successfully!");
    }

    private void createAdminRole() {
        User user = new User();
        user.setUsername("admin");
        user.setActive(true);
        user.setEmail("admin@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(roleRepository.findByName("ADMIN").get());
        user.setIsEmailValid(true);
        userRepository.save(user);
        walletService.createWallet(user);
        System.out.println("Create Admin Role");
    }

    private void createConsumerRole() {
        User user = new User();
        user.setUsername("consumer");
        user.setActive(true);
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail("consumer@gmail.com");
        user.setIsEmailValid(true);
        user.setRole(roleRepository.findByName("CONSUMER").get());

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setAmount(1000000000);

        BankAccount bankAccount = new BankAccount();
        bankAccount.setBankName("Techcombank");
        bankAccount.setAccountHolderName("Le Thi B");
        bankAccount.setAccountNumber("987654321");
        bankAccount.setUser(user);

        userRepository.save(user);
        bankAccountRepository.save(bankAccount);
        walletRepository.save(wallet);
        System.out.println("Khoi tao consumer");
    }

    private void createConsumerTypes() {
        ConsumerType t1 = new ConsumerType();
        t1.setName("Student");
        consumerTypeRepository.save(t1);

        ConsumerType t2 = new ConsumerType();
        t2.setName("Researcher");
        consumerTypeRepository.save(t2);

        ConsumerType t3 = new ConsumerType();
        t3.setName("Educator");
        consumerTypeRepository.save(t3);

        ConsumerType t4 = new ConsumerType();
        t4.setName("Startup");
        consumerTypeRepository.save(t4);

        ConsumerType t5 = new ConsumerType();
        t5.setName("Business");
        consumerTypeRepository.save(t5);
        System.out.println("Khoi tao consumer type");
    }

    private void initVietnamLocations() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("location.json");
            if (is == null) {
                System.out.println("Không tìm thấy file location.json trong resources!");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(is, Map.class);

            // ---- Insert Provinces ----
            List<Map<String, String>> provinces = (List<Map<String, String>>) data.get("province");
            if (provinceRepository.count() == 0 && provinces != null) {
                List<Province> provinceEntities = provinces.stream().map(p -> {
                    Province province = new Province();
                    province.setIdProvince(p.get("idProvince"));
                    province.setName(p.get("name"));
                    return province;
                }).toList();
                provinceRepository.saveAll(provinceEntities);
                System.out.println("Đã khởi tạo " + provinceEntities.size() + " tỉnh/thành.");
            }

            // ---- Insert Communes ----
            List<Map<String, String>> communes = (List<Map<String, String>>) data.get("commune");
            if (communeRepository.count() == 0 && communes != null) {
                List<Commune> communeEntities = new ArrayList<>();
                for (Map<String, String> c : communes) {
                    String idProvince = c.get("idProvince");
                    Province province = provinceRepository.findById(idProvince).orElse(null);
                    if (province == null) continue;

                    Commune commune = new Commune();
                    commune.setIdCommune(c.get("idCommune"));
                    commune.setName(c.get("name"));
                    commune.setProvince(province);
                    communeEntities.add(commune);
                }
                communeRepository.saveAll(communeEntities);
                System.out.println("Đã khởi tạo " + communeEntities.size() + " xã/phường.");
            }

            System.out.println("🎉 Dữ liệu địa lý Việt Nam đã được khởi tạo thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
