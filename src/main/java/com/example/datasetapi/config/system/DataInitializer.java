package com.example.datasetapi.config.system;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.enums.DocumentType;
import com.example.datasetapi.enums.UserStatus;
import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.enums.VerificationStatus.VerificationStatus;
import com.example.datasetapi.enums.Datasets.FileExtension;
import com.example.datasetapi.enums.Datasets.DatasetInforStatus;
import com.example.datasetapi.model.Dataset.*;
import com.example.datasetapi.model.userManager.*;
import com.example.datasetapi.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.ConsumerTypeRepository;
import com.example.datasetapi.repository.RoleRepository;
import com.example.datasetapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private  RoleRepository roleRepository;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  ConsumerTypeRepository consumerTypeRepository;
@Autowired
    private  ProviderRegistrationRepository providerRegistrationRepository;
@Autowired
private  ProviderIndentityDocumentRepository providerIndentityDocumentRepository;
@Autowired
private  CategoryRepository categoryRepository;
@Autowired
private  DatasetTypeRepository datasetTypeRepository;
@Autowired
private  Dataset_Type_Column_Repository datasetTypeColumnRepository;
@Autowired
private  DatasetRepository datasetRepository;
@Autowired
private  ProviderRepository providerRepository;
@Autowired
private  DatasetInforRepository datasetInforRepository;
@Autowired
private PricingRuleRepo pricingRuleRepo;

    @Override
    public void run(String... args) throws Exception {
    if(userRepository.count()==0){
        roleRepository.save(new Role("ADMIN"));
        roleRepository.save(new Role("PROVIDER"));
        roleRepository.save(new Role("CONSUMER"));
        roleRepository.save(new Role("MODERATOR"));
        System.out.println("Roles & permissions initialized.");

        createConsumerRole();

        createAdminRole();

        createConsumerTypes();

        createModerator();
        System.out.println("Seeded roles, users, and consumer types.");
        //tao va gan provider dang ky mau tranh trung lap thong tin khi create-drop db
        createProviderRegistration();


        createCategory();


        createDatasetType();

        createDataset_Type_Columns();

        assignColumnAndCategoryToDatasetType();

        createDatasetDemo();
        createModerationTestData();
        createPricingRule();

    }
    else {
        System.out.println("Data already Init............" +
                "======================================Stop Init Data========================================\n");

    }
        System.out.println("===========================Load Data success===========================\n");
    }

    private void createPricingRule() {
        List<PricingRule> rules = new ArrayList<>();

        PricingRule r1 = new PricingRule();
        r1.setMethod(PricingMethod.ONE_TIME);
        r1.setPlanName("Small Pack");
        r1.setMinRow(1000L);
        r1.setMaxRow(10000L);
        r1.setBasePricePerRowVnd(5.0);
        r1.setAllowOverage(false);
        r1.setProviderShare(40);
        r1.setPlatformShare(60);
        r1.setDatasetPack(DatasetPack.SMALL);
        r1.setNote("Small package for low row usage");
        rules.add(r1);

        PricingRule r2 = new PricingRule();
        r2.setMethod(PricingMethod.ONE_TIME);
        r2.setPlanName("Medium Pack");
        r2.setMinRow(10001L);
        r2.setMaxRow(100000L);
        r2.setBasePricePerRowVnd(4.0);
        r2.setDiscountPercent(5);
        r2.setAllowOverage(false);
        r2.setProviderShare(35);
        r2.setPlatformShare(65);
        r2.setDatasetPack(DatasetPack.MEDIUM);
        r2.setNote("Medium package for moderate row usage");
        rules.add(r2);

        PricingRule r3 = new PricingRule();
        r3.setMethod(PricingMethod.ONE_TIME);
        r3.setPlanName("Big Pack");
        r3.setMinRow(100001L);
        r3.setMaxRow(1000000L);
        r3.setBasePricePerRowVnd(3.0);
        r3.setDiscountPercent(10);
        r3.setAllowOverage(false);
        r3.setProviderShare(30);
        r3.setPlatformShare(70);
        r3.setDatasetPack(DatasetPack.LARGE);
        r3.setNote("Big package for large row usage");
        rules.add(r3);

        PricingRule r4 = new PricingRule();
        r4.setMethod(PricingMethod.SUBSCRIPTION);
        r4.setPlanName("Basic Monthly");
        r4.setBasePricePoint(200.0);
        r4.setRowLimit(50000L);
        r4.setTimeLimitDay(30);
        r4.setExtraCostPer1kPoint(2000.0);
        r4.setAllowOverage(true);
        r4.setProviderShare(40);
        r4.setPlatformShare(60);
        r4.setNote("30-day subscription package");
        rules.add(r4);

        PricingRule r5 = new PricingRule();
        r5.setMethod(PricingMethod.SUBSCRIPTION);
        r5.setPlanName("Pro 2 Months");
        r5.setBasePricePoint(500.0);
        r5.setRowLimit(200000L);
        r5.setTimeLimitDay(60);
        r5.setExtraCostPer1kPoint(1500.0);
        r5.setDiscountPercent(5);
        r5.setAllowOverage(true);
        r5.setProviderShare(35);
        r5.setPlatformShare(65);
        r5.setNote("2-month subscription package");
        rules.add(r5);

        PricingRule r6 = new PricingRule();
        r6.setMethod(PricingMethod.SUBSCRIPTION);
        r6.setPlanName("Premium Yearly");
        r6.setBasePricePoint(1000.0);
        r6.setRowLimit(600000L);
        r6.setTimeLimitDay(365);
        r6.setExtraCostPer1kPoint(1000.0);
        r6.setDiscountPercent(10);
        r6.setAllowOverage(true);
        r6.setProviderShare(30);
        r6.setPlatformShare(70);
        r6.setNote("1-year subscription package");
        rules.add(r6);

        PricingRule r7 = new PricingRule();
        r7.setMethod(PricingMethod.API);
        r7.setPlanName("Starter 10K Call");
        r7.setBasePricePoint(500.0);
        r7.setRequestLimit(10000L);
        r7.setAllowOverage(false);
        r7.setProviderShare(40);
        r7.setPlatformShare(60);
        r7.setNote("API package with 10K requests");
        rules.add(r7);

        PricingRule r8 = new PricingRule();
        r8.setMethod(PricingMethod.API);
        r8.setPlanName("Pro 100K Call");
        r8.setBasePricePoint(3000.0);
        r8.setRequestLimit(100000L);
        r8.setDiscountPercent(5);
        r8.setAllowOverage(false);
        r8.setProviderShare(35);
        r8.setPlatformShare(65);

        r8.setNote("API package with 100K requests");
        rules.add(r8);

        PricingRule r9 = new PricingRule();
        r9.setMethod(PricingMethod.API);
        r9.setPlanName("Enterprise 1M Call");
        r9.setBasePricePoint(20000.0);
        r9.setRequestLimit(1000000L);
        r9.setDiscountPercent(10);
        r9.setAllowOverage(false);
        r9.setProviderShare(30);
        r9.setPlatformShare(70);
        r9.setNote("API package with 1M requests");
        rules.add(r9);

        pricingRuleRepo.saveAll(rules);
    }


    private void createModerator() {
        User user = new User();
        user.setUsername("moderator");
        user.setPassword(passwordEncoder.encode("moderator"));
        user.setRole(roleRepository.findByName("MODERATOR").get());
        user.setEmail("moderator@gmail.com");
        userRepository.save(user);
    }

    private void createModerationTestData() {
        String basePath = Paths.get("ev_station_mixed_errors.csv").toString();
        if (Files.exists(Paths.get(basePath))) {
            System.out.println("Found file at: " + basePath);
        } else {
            System.out.println("File not found!");
        }

        DatasetType marketOverview = datasetTypeRepository.findByName("EV_Station_Market_Overview");
        if (marketOverview == null) return;

        List<DatasetInformation> datasetList = new ArrayList<>();

        DatasetInformation ds1 = new DatasetInformation();
        ds1.setName("ev_station_mixed_errors");
        ds1.setDatasetExtension(FileExtension.csv);
        ds1.setStatus(DatasetInforStatus.PENDING);
        ds1.setFile_url(basePath);
        ds1.setRowCount(100L);
        ds1.setDatasetType(marketOverview);
        Provider provider = providerRepository.findById(4L).get();
        Address address = new Address();
        address.setProvince("test province");
        address.setWard("test ward");
        address.setDistrict("test district");
        ds1.setAddress(address);
        ds1.setProvider(provider);
        datasetList.add(ds1);

        datasetInforRepository.saveAll(datasetList);
        System.out.println("Seeded dataset_information test entries for moderation.");
    }

    private void createDatasetDemo() {
        DatasetGroup datasetGroup = new DatasetGroup();
        datasetGroup.setProvider(providerRepository.findById(4L).get());
        datasetGroup.setDatasetType(datasetTypeRepository.getOne(1L));

        Dataset dataset = new Dataset();


        dataset.setFileKey("testUpload.txt");
        dataset.setName("testDataset");
        dataset.setFileKey("testUpload.txt");
        dataset.setDescription("testDatasetDescription");
        dataset.setDatasetGroup(datasetGroup);

        // Save first to get the ID
        datasetRepository.save(dataset);

    }

    private void assignColumnAndCategoryToDatasetType() {
        // Dataset 1: EV_Station_Location_Basic
        DatasetType locationBasic = datasetTypeRepository.findByName("EV_Station_Location_Basic");
        if (locationBasic != null) {
            List<DatasetTypeColumn> locationBasicCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("Station_ID", "Latitude_Longitude", "Address")
            );
            List<Category> locationBasicCategories = categoryRepository.findByNameIn(
                    Arrays.asList("Location")
            );
            locationBasic.setDatasetTypeColumnList(locationBasicCols);
            datasetTypeRepository.save(locationBasic);
        }

        // Dataset 2: EV_Station_Geo_Usage
        DatasetType geoUsage = datasetTypeRepository.findByName("EV_Station_Geo_Usage");
        if (geoUsage != null) {
            List<DatasetTypeColumn> geoUsageCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("Station_ID", "Latitude_Longitude", "Daily_Sessions", "Energy_Delivered_kWh")
            );
            List<Category> geoUsageCategories = categoryRepository.findByNameIn(
                    Arrays.asList("Location", "Operation")
            );
            geoUsage.setDatasetTypeColumnList(geoUsageCols);
            datasetTypeRepository.save(geoUsage);
        }

        // Dataset 3: EV_Tech_Capacity
        DatasetType techCapacity = datasetTypeRepository.findByName("EV_Tech_Capacity");
        if (techCapacity != null) {
            List<DatasetTypeColumn> techCapacityCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("Station_ID", "Connector_Type", "Max_Power_kW", "Number_of_Connectors", "Status")
            );
            List<Category> techCapacityCategories = categoryRepository.findByNameIn(
                    Arrays.asList("Technical", "Operation")
            );
            techCapacity.setDatasetTypeColumnList(techCapacityCols);
            datasetTypeRepository.save(techCapacity);
        }

        // Dataset 4: EV_Station_Market_Overview
        DatasetType marketOverview = datasetTypeRepository.findByName("EV_Station_Market_Overview");
        if (marketOverview != null) {
            List<DatasetTypeColumn> marketOverviewCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("Station_ID", "Provider", "Address", "Connector_Type", "Pricing_Model", "Price_per_kWh")
            );
            List<Category> marketOverviewCategories = categoryRepository.findByNameIn(
                    Arrays.asList("Location", "Technical", "Pricing & Payment")
            );
            marketOverview.setDatasetTypeColumnList(marketOverviewCols);
            datasetTypeRepository.save(marketOverview);
        }

        // Dataset 5: EV_User_Behavior_Summary
        DatasetType userBehavior = datasetTypeRepository.findByName("EV_User_Behavior_Summary");
        if (userBehavior != null) {
            List<DatasetTypeColumn> userBehaviorCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("User_ID_Hash", "Vehicle_Type", "Charging_Frequency", "Avg_Charging_Time", "Preferred_Station_ID")
            );
            List<Category> userBehaviorCategories = categoryRepository.findByNameIn(
                    Arrays.asList("User Behavior", "Operation")
            );
            userBehavior.setDatasetTypeColumnList(userBehaviorCols);
            datasetTypeRepository.save(userBehavior);
        }

        // Dataset 6: EV_Pricing_Analytics
        DatasetType pricingAnalytics = datasetTypeRepository.findByName("EV_Pricing_Analytics");
        if (pricingAnalytics != null) {
            List<DatasetTypeColumn> pricingAnalyticsCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("Station_ID", "Pricing_Model", "Price_per_kWh", "Payment_Methods")
            );
            List<Category> pricingAnalyticsCategories = categoryRepository.findByNameIn(
                    Arrays.asList("Pricing & Payment")
            );
            pricingAnalytics.setDatasetTypeColumnList(pricingAnalyticsCols);
            datasetTypeRepository.save(pricingAnalytics);
        }

        // Dataset 7: EV_Station_Performance_Trend
        DatasetType performanceTrend = datasetTypeRepository.findByName("EV_Station_Performance_Trend");
        if (performanceTrend != null) {
            List<DatasetTypeColumn> performanceTrendCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("Station_ID", "Monthly_Sessions", "Monthly_Energy_kWh", "Peak_Hours")
            );
            List<Category> performanceTrendCategories = categoryRepository.findByNameIn(
                    Arrays.asList("Operation")
            );
            performanceTrend.setDatasetTypeColumnList(performanceTrendCols);
            datasetTypeRepository.save(performanceTrend);
        }

        // Dataset 8: EV_All_in_One
        DatasetType allInOne = datasetTypeRepository.findByName("EV_All_in_One");
        if (allInOne != null) {
            List<DatasetTypeColumn> allInOneCols = datasetTypeColumnRepository.findAll();
            allInOne.setDatasetTypeColumnList(allInOneCols);
            datasetTypeRepository.save(allInOne);
        }
    }



    private void createDataset_Type_Columns() {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Station_ID");
        columnNames.add("Latitude_Longitude");
        columnNames.add("Address");
        columnNames.add("Daily_Sessions");
        columnNames.add("Energy_Delivered_kWh");
        columnNames.add("Connector_Type");
        columnNames.add("Max_Power_kW");
        columnNames.add("Provider");
        columnNames.add("Address");
        columnNames.add("Connector_Type");
        columnNames.add("Pricing_Model");
        columnNames.add("Price_per_kWh");
        columnNames.add("User_ID_Hash");
        columnNames.add("Vehicle_Type");
        columnNames.add("Charging_Frequency");
        columnNames.add("Avg_Charging_Time");
        columnNames.add("Preferred_Station_ID");
        columnNames.add("Payment_Methods");
        columnNames.add("Monthly_Sessions");
        columnNames.add("Monthly_Energy_kWh");
        columnNames.add("Peak_Hours");

        for (String name : columnNames) {
            if (!datasetTypeColumnRepository.existsByColumnName(name)) {
                DatasetTypeColumn c = new DatasetTypeColumn();
                c.setColumnName(name);
                datasetTypeColumnRepository.save(c);
            }
        }
    }




    private void createDatasetType() {

        List<Category> categories = new ArrayList<>();

        // 🟢 1. EV_Station_Location_Basic
        DatasetType datasetType1 = new DatasetType();
        datasetType1.setName("EV_Station_Location_Basic");
        categories = categoryRepository.findAllByNameIn(List.of("Location"));
        datasetType1.setCategories(categories);
        datasetTypeRepository.save(datasetType1);

        // 🟢 2. EV_Station_Geo_Usage
        DatasetType datasetType2 = new DatasetType();
        datasetType2.setName("EV_Station_Geo_Usage");
        categories = categoryRepository.findAllByNameIn(List.of("Location", "Operation"));
        datasetType2.setCategories(categories);
        datasetTypeRepository.save(datasetType2);

        // 🟢 3. EV_Tech_Capacity
        DatasetType datasetType3 = new DatasetType();
        datasetType3.setName("EV_Tech_Capacity");
        categories = categoryRepository.findAllByNameIn(List.of("Technical", "Operational"));
        datasetType3.setCategories(categories);
        datasetTypeRepository.save(datasetType3);

        // 🟢 4. EV_Station_Market_Overview
        DatasetType datasetType4 = new DatasetType();
        datasetType4.setName("EV_Station_Market_Overview");
        categories = categoryRepository.findAllByNameIn(List.of("Location", "Technical", "Pricing & Payment"));
        datasetType4.setCategories(categories);
        datasetTypeRepository.save(datasetType4);

        // 🟢 5. EV_User_Behavior_Summary
        DatasetType datasetType5 = new DatasetType();
        datasetType5.setName("EV_User_Behavior_Summary");
        categories = categoryRepository.findAllByNameIn(List.of("User Behavior", "Operational"));
        datasetType5.setCategories(categories);
        datasetTypeRepository.save(datasetType5);

        // 🟢 6. EV_Pricing_Analytics
        DatasetType datasetType6 = new DatasetType();
        datasetType6.setName("EV_Pricing_Analytics");
        categories = categoryRepository.findAllByNameIn(List.of("Pricing & Payment"));
        datasetType6.setCategories(categories);
        datasetTypeRepository.save(datasetType6);

        // 🟢 7. EV_Station_Performance_Trend
        DatasetType datasetType7 = new DatasetType();
        datasetType7.setName("EV_Station_Performance_Trend");
        categories = categoryRepository.findAllByNameIn(List.of("Operational"));
        datasetType7.setCategories(categories);
        datasetTypeRepository.save(datasetType7);

        // 🟢 8. EV_All_in_One
        DatasetType datasetType8 = new DatasetType();
        datasetType8.setName("EV_All_in_One");
        categories = categoryRepository.findAllByNameIn(List.of(
                "Location", "Technical", "Operational", "User Behavior", "Pricing & Payment"
        ));
        datasetType8.setCategories(categories);
        datasetTypeRepository.save(datasetType8);
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

        for(String cate : categories){
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
        registration.setAddressLine("123 Le Loi");
        registration.setCity("Binh Duong");
        registration.setDistrict("Thu Dau Mot");
        registration.setWard("Ward 1");
        registration.setRegistrationStatus(RegistrationStatus.PENDING);
        registration.setCreatedAt(Instant.now());
        registration.setUpdatedAt(Instant.now());

        // 2️⃣ Tạo document1
        ProviderIdentityDocument doc1 = new ProviderIdentityDocument();
        doc1.setProvider(registration);
        doc1.setImage_url("https://res.cloudinary.com/dofuoy88z/image/upload/v1758869860/seridykcpf1ospeni0zs.jpg");
        doc1.setUploadedAt(Instant.now());
        doc1.setIdCardVerificationStatus(VerificationStatus.PENDING);
        doc1.setManager_id(0L);
        doc1.setIdCardRetentionExpiry(Instant.now().plusSeconds(60*60*24*365));
        doc1.setDocumentType(DocumentType.CCCD_FRONT);

        // 3️⃣ Tạo document2
        ProviderIdentityDocument doc2 = new ProviderIdentityDocument();
        doc2.setProvider(registration);
        doc2.setImage_url("https://res.cloudinary.com/dofuoy88z/image/upload/v1758869861/oegnzrkytqhvfexxfdrf.jpg");
        doc2.setUploadedAt(Instant.now());
        doc2.setIdCardVerificationStatus(VerificationStatus.PENDING);
        doc2.setManager_id(0L);
        doc2.setIdCardRetentionExpiry(Instant.now().plusSeconds(60*60*24*365));
        doc2.setDocumentType(DocumentType.CCCD_BACK);

        registration.setIdentityDocuments(List.of(doc1, doc2));

        // 4️⃣ Lưu ProviderRegistration (Hibernate cascade sẽ lưu cả document)

        // 5️⃣ Tạo User cho Provider
        User user = new User();
        user.setUserStatus(UserStatus.ACTIVE);
        user.setRole(roleRepository.getRolesByName("PROVIDER"));
        user.setEmail("provider@gmail.com");
        user.setUsername("provider");
        user.setRole(roleRepository.getRolesByName("PROVIDER"));
        user.setPassword(passwordEncoder.encode("password"));

        //tao address gan cho provider de test thu mau
        Address address = new Address();
        address.setDistrict("test district");
        address.setWard("test ward");
        address.setProvince("test province");

        // 6️⃣ Tạo Provider, gán Registration và User
        Provider provider = new Provider();
        provider.setProviderRegistration(registration); // registration đã managed trong transaction
        provider.setUser(user);
        provider.setBankAccount("123456789");
        provider.setAddresses(List.of(address));

        providerRepository.save(provider);
    System.out.println("khoi tao provider");
    }



    private void createAdminRole() {
        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(roleRepository.findByName("ADMIN").get());
        userRepository.save(user);
        System.out.println("khoi tao admin ");
    }

    private void  createConsumerRole() {
        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail("consumer@gmail.com");
        user.setRole(roleRepository.findByName("CONSUMER").get());
        userRepository.save(user);
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
}
