package com.example.datasetapi.config.system;

import com.example.datasetapi.enums.DocumentType;
import com.example.datasetapi.enums.UserStatus;
import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.enums.VerificationStatus.VerificationStatus;
import com.example.datasetapi.enums.Datasets.FileExtension;
import com.example.datasetapi.enums.Datasets.DatasetInforStatus;
import com.example.datasetapi.model.Dataset.Category;
import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.model.Dataset.DatasetTypeColumn;
import com.example.datasetapi.model.userManager.*;
import com.example.datasetapi.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.datasetapi.model.userManager.User;
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
    private final DatasetInforRepository datasetInforRepository;

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
                           ConsumerTypeRepository consumerTypeRepository,
                           DatasetInforRepository datasetInforRepository) {
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
        this.datasetInforRepository = datasetInforRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        roleRepository.save(new Role("ADMIN"));
        roleRepository.save(new Role("CONSUMER"));
        System.out.println("Roles & permissions initialized.");
        createConsumerRole();
        createAdminRole();
        createConsumerTypes();
        System.out.println("Seeded roles, users, and consumer types.");
        createProviderRegistration();
        createCategory();
        createDatasetType();
        createDataset_Type_Columns();
        assignColumnAndCategoryToDatasetType();
        createDatasetDemo();
        createModerationTestData();
    }

    private void createModerationTestData() {
        String basePath = "https://drive.google.com/drive/folders/1rYg93Jl0s1wOwSP6GUURf8md1tFEnxaV";

        DatasetType marketOverview = datasetTypeRepository.findByName("EV_Station_Market_Overview");
        if (marketOverview == null) return;

        List<DatasetInformation> datasetList = new ArrayList<>();

        DatasetInformation ds1 = new DatasetInformation();
        ds1.setName("EV_Station_Pass_1");
        ds1.setDatasetExtension(FileExtension.csv);
        ds1.setStatus(DatasetInforStatus.PENDING);
        ds1.setFile_url(basePath); // đổi ID cho đúng file thật
        ds1.setRowCount(100L);
        ds1.setDatasetType(marketOverview);
        datasetList.add(ds1);

        datasetInforRepository.saveAll(datasetList);
        System.out.println("Seeded dataset_information test entries for moderation.");
    }

    private void createDatasetDemo() {
        Dataset dataset = new Dataset();
        dataset.setDatasetType(datasetTypeRepository.findById(1L).get());
        dataset.setFileKey("testUpload.txt");
        dataset.setName("testDataset");

        Dataset savedDataset = datasetRepository.save(dataset);
        List<Category> categories = new ArrayList<>();
        categories.add(categoryRepository.findById(1).get());
        savedDataset.setCategories(categories);
        datasetRepository.save(savedDataset);
    }

    private void assignColumnAndCategoryToDatasetType() {
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

        DatasetType geoUsage = datasetTypeRepository.findByName("EV_Station_Geo_Usage");
        if (geoUsage != null) {
            List<DatasetTypeColumn> geoUsageCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("Station_ID", "Latitude_Longitude", "Daily_Sessions", "Energy_Delivered_kWh")
            );
            geoUsage.setDatasetTypeColumnList(geoUsageCols);
            datasetTypeRepository.save(geoUsage);
        }

        DatasetType techCapacity = datasetTypeRepository.findByName("EV_Tech_Capacity");
        if (techCapacity != null) {
            List<DatasetTypeColumn> techCapacityCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("Station_ID", "Connector_Type", "Max_Power_kW", "Number_of_Connectors", "Status")
            );
            techCapacity.setDatasetTypeColumnList(techCapacityCols);
            datasetTypeRepository.save(techCapacity);
        }

        DatasetType marketOverview = datasetTypeRepository.findByName("EV_Station_Market_Overview");
        if (marketOverview != null) {
            List<DatasetTypeColumn> marketOverviewCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("Station_ID", "Provider", "Address", "Connector_Type", "Pricing_Model", "Price_per_kWh")
            );
            marketOverview.setDatasetTypeColumnList(marketOverviewCols);
            datasetTypeRepository.save(marketOverview);
        }

        DatasetType userBehavior = datasetTypeRepository.findByName("EV_User_Behavior_Summary");
        if (userBehavior != null) {
            List<DatasetTypeColumn> userBehaviorCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("User_ID_Hash", "Vehicle_Type", "Charging_Frequency", "Avg_Charging_Time", "Preferred_Station_ID")
            );
            userBehavior.setDatasetTypeColumnList(userBehaviorCols);
            datasetTypeRepository.save(userBehavior);
        }

        DatasetType pricingAnalytics = datasetTypeRepository.findByName("EV_Pricing_Analytics");
        if (pricingAnalytics != null) {
            List<DatasetTypeColumn> pricingAnalyticsCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("Station_ID", "Pricing_Model", "Price_per_kWh", "Payment_Methods")
            );
            pricingAnalytics.setDatasetTypeColumnList(pricingAnalyticsCols);
            datasetTypeRepository.save(pricingAnalytics);
        }

        DatasetType performanceTrend = datasetTypeRepository.findByName("EV_Station_Performance_Trend");
        if (performanceTrend != null) {
            List<DatasetTypeColumn> performanceTrendCols = datasetTypeColumnRepository.findByColumnNameIn(
                    Arrays.asList("Station_ID", "Monthly_Sessions", "Monthly_Energy_kWh", "Peak_Hours")
            );
            performanceTrend.setDatasetTypeColumnList(performanceTrendCols);
            datasetTypeRepository.save(performanceTrend);
        }

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
        List<String> datasetTypes = new ArrayList<>();
        datasetTypes.add("EV_Station_Location_Basic");
        datasetTypes.add("EV_Station_Geo_Usage");
        datasetTypes.add("EV_Tech_Capacity");
        datasetTypes.add("EV_Station_Market_Overview");
        datasetTypes.add("EV_User_Behavior_Summary");
        datasetTypes.add("EV_Pricing_Analytics");
        datasetTypes.add("EV_Station_Performance_Trend");
        datasetTypes.add("EV_All_in_One");

        for (String datasetT : datasetTypes) {
            DatasetType datasetType = new DatasetType();
            datasetType.setName(datasetT);
            datasetTypeRepository.save(datasetType);
        }
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
    }

    @Transactional
    protected void createProviderRegistration() {
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

        ProviderIdentityDocument doc1 = new ProviderIdentityDocument();
        doc1.setProvider(registration);
        doc1.setImage_url("https://res.cloudinary.com/dofuoy88z/image/upload/v1758869860/seridykcpf1ospeni0zs.jpg");
        doc1.setUploadedAt(Instant.now());
        doc1.setIdCardVerificationStatus(VerificationStatus.PENDING);
        doc1.setManager_id(0L);
        doc1.setIdCardRetentionExpiry(Instant.now().plusSeconds(60 * 60 * 24 * 365));
        doc1.setDocumentType(DocumentType.CCCD_FRONT);

        ProviderIdentityDocument doc2 = new ProviderIdentityDocument();
        doc2.setProvider(registration);
        doc2.setImage_url("https://res.cloudinary.com/dofuoy88z/image/upload/v1758869861/oegnzrkytqhvfexxfdrf.jpg");
        doc2.setUploadedAt(Instant.now());
        doc2.setIdCardVerificationStatus(VerificationStatus.PENDING);
        doc2.setManager_id(0L);
        doc2.setIdCardRetentionExpiry(Instant.now().plusSeconds(60 * 60 * 24 * 365));
        doc2.setDocumentType(DocumentType.CCCD_BACK);

        registration.setIdentityDocuments(List.of(doc1, doc2));

        User user = new User();
        user.setUserStatus(UserStatus.ACTIVE);
        user.setRole(roleRepository.getRolesByName("PROVIDER"));
        user.setEmail("provider@gmail.com");
        user.setUsername("provider");
        user.setPassword(passwordEncoder.encode("password"));

        Provider provider = new Provider();
        provider.setProviderRegistration(registration);
        provider.setUser(user);
        provider.setBankAccount("123456789");
        providerRepository.save(provider);
    }

    private void createAdminRole() {
        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(roleRepository.findByName("ADMIN").get());
        userRepository.save(user);
    }

    private void createConsumerRole() {
        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail("consumer@gmail.com");
        user.setRole(roleRepository.findByName("CONSUMER").get());
        userRepository.save(user);
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
    }
}
