package com.example.datasetapi.config.system;

import com.example.datasetapi.enums.DocumentType;
import com.example.datasetapi.enums.UserStatus;
import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.enums.VerificationStatus.VerificationStatus;
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

import java.time.Instant;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ConsumerTypeRepository consumerTypeRepository;

    private final ProviderRegistrationRepository providerRegistrationRepository;
    private final ProviderIndentityDocumentRepository providerIndentityDocumentRepository;
    private final CategoryRepository categoryRepository;
    private final DatasetTypeRepository datasetTypeRepository;
    private final Dataset_Type_Column_Repository datasetTypeColumnRepository;
    private final DatasetRepository datasetRepository;
    private final ProviderRepository providerRepository;

    @Autowired
    public DataInitializer(DatasetRepository datasetRepository,
                           Dataset_Type_Column_Repository datasetTypeColumnRepository,
                           DatasetTypeRepository datasetTypeRepository,
                           CategoryRepository categoryRepository,
                           ProviderIndentityDocumentRepository providerIndentityDocumentRepository,
                           ProviderRegistrationRepository providerRegistrationRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           ProviderRepository providerRepository,
                           ConsumerTypeRepository consumerTypeRepository) {
        this.datasetRepository = datasetRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.consumerTypeRepository = consumerTypeRepository;
        this.providerRegistrationRepository = providerRegistrationRepository;
        this.providerIndentityDocumentRepository = providerIndentityDocumentRepository;
    this.categoryRepository = categoryRepository;
    this.datasetTypeRepository = datasetTypeRepository;
    this.datasetTypeColumnRepository = datasetTypeColumnRepository;
        this.providerRepository = providerRepository;
    }


    @Override
    public void run(String... args) throws Exception {

      roleRepository.save(new Role("ADMIN"));
    roleRepository.save(new Role("PROVIDER"));
        roleRepository.save(new Role("CONSUMER"));
        System.out.println("Roles & permissions initialized.");

        createConsumerRole();

        createAdminRole();

        createConsumerTypes();

        System.out.println("Seeded roles, users, and consumer types.");
        //tao va gan provider dang ky mau tranh trung lap thong tin khi create-drop db
        createProviderRegistration();


        createCategory();




        createDatasetType();

        createDataset_Type_Columns();

        assignColumnAndCategoryToDatasetType();

        createDatasetDemo();
    }

    private void createDatasetDemo() {
        DatasetGroup datasetGroup = new DatasetGroup();
        datasetGroup.setProvider(providerRepository.findById(3L).get());
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
            List<DatasetTypeColumn> allInOneCols = datasetTypeColumnRepository.findAll(); // toàn bộ cột
            List<Category> allInOneCategories = categoryRepository.findByNameIn(
                    Arrays.asList("All")
            );
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
@Transactional
protected void createProviderRegistration() {
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
