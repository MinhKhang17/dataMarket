package com.example.datasetapi.config.system;

import com.example.datasetapi.enums.DocumentType;
import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.enums.VerificationStatus.VerificationStatus;
import com.example.datasetapi.model.Dataset.Category;
import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.model.Dataset.DatasetTypeColumn;
import com.example.datasetapi.model.UserManager.ProviderIdentityDocument;
import com.example.datasetapi.model.UserManager.ProviderRegistration;
import com.example.datasetapi.model.UserManager.Role;
import com.example.datasetapi.model.UserManager.User;
import com.example.datasetapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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
private final ProviderRegistrationRepository providerRegistrationRepository;
private final ProviderIndentityDocumentRepository providerIndentityDocumentRepository;
private final CategoryRepository categoryRepository;
private final DatasetTypeRepository datasetTypeRepository;
private final Dataset_Type_Column_Repository datasetTypeColumnRepository;
private final DatasetRepository datasetRepository;
    @Autowired
    public DataInitializer(DatasetRepository datasetRepository,Dataset_Type_Column_Repository datasetTypeColumnRepository,DatasetTypeRepository datasetTypeRepository,CategoryRepository categoryRepository, ProviderIndentityDocumentRepository providerIndentityDocumentRepository,ProviderRegistrationRepository providerRegistrationRepository,UserRepository userRepository,RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.datasetRepository = datasetRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.providerRegistrationRepository = providerRegistrationRepository;
        this.providerIndentityDocumentRepository = providerIndentityDocumentRepository;
    this.categoryRepository = categoryRepository;
    this.datasetTypeRepository = datasetTypeRepository;
    this.datasetTypeColumnRepository = datasetTypeColumnRepository;
    }


    @Override
    public void run(String... args) throws Exception {

      roleRepository.save(new Role("ADMIN"));

        roleRepository.save(new Role("CONSUMER"));

        System.out.println("Roles & permissions initialized.");

        createConsumerRole();

        createAdminRole();
        //tao va gan provider dang ky mau tranh trung lap thong tin khi create-drop db
        createProviderRegistration();

        createCategory();


        createDatasetType();

        createDataset_Type_Columns();

        assignColumnAndCategoryToDatasetType();

        createDatasetDemo();
    }

    private void createDatasetDemo() {
        Dataset dataset = new Dataset();
        dataset.setDatasetType(datasetTypeRepository.findById(1L).get());
        dataset.setFileKey("testUpload.txt");
    List<Category> categories = new ArrayList<>();
    categories.add(categoryRepository.findById(1).get());

        dataset.setCategories(categories);
        dataset.setName("testDataset");
        dataset.setDescription("This is a description");
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
        for(String columnName : columnNames){
            DatasetTypeColumn datasetTypeColumn = new DatasetTypeColumn();
            datasetTypeColumn.setColumnName(columnName);
            datasetTypeColumnRepository.save(datasetTypeColumn);
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

        for(String datasetT:datasetTypes){
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

        for(String cate : categories){
            Category newCategory = new Category();
            newCategory.setName(cate);
            categoryRepository.save(newCategory);
        }
    }

    private void createProviderRegistration() {
        ProviderRegistration provider = new ProviderRegistration();
        provider.setFullName("Nguyen Van A");
        provider.setEmail("nguyenvana@example.com");
        provider.setPhoneNumber("0123456789");
        provider.setAddressLine("123 Le Loi");
        provider.setCity("Binh Duong");
        provider.setDistrict("Thu Dau Mot");
        provider.setWard("Ward 1");
        provider.setRegistrationStatus(RegistrationStatus.PENDING);
        provider.setCreatedAt(Instant.now());
        provider.setUpdatedAt(Instant.now());

        // Tạo document1
        ProviderIdentityDocument document1 = new ProviderIdentityDocument();
        document1.setProvider(provider);
        document1.setImage_url("https://res.cloudinary.com/dofuoy88z/image/upload/v1758869860/seridykcpf1ospeni0zs.jpg");
        document1.setUploadedAt(Instant.now());
        document1.setIdCardVerificationStatus(VerificationStatus.PENDING);
        document1.setManager_id(0L);
        document1.setIdCardRetentionExpiry(Instant.now().plusSeconds(60*60*24*365));
        document1.setDocumentType(DocumentType.CCCD_FRONT);

        // Tạo document2
        ProviderIdentityDocument document2 = new ProviderIdentityDocument();
        document2.setProvider(provider);
        document2.setImage_url("https://res.cloudinary.com/dofuoy88z/image/upload/v1758869861/oegnzrkytqhvfexxfdrf.jpg");
        document2.setUploadedAt(Instant.now());
        document2.setIdCardVerificationStatus(VerificationStatus.PENDING);
        document2.setManager_id(0L);
        document2.setIdCardRetentionExpiry(Instant.now().plusSeconds(60*60*24*365));
        document2.setDocumentType(DocumentType.CCCD_BACK);

        // Gán list document cho provider
        provider.setIdentityDocuments(List.of(document1, document2));

        // Lưu provider, Hibernate sẽ tự lưu cả document (nếu cascade ALL)
        providerRegistrationRepository.save(provider);
    }



    private void createAdminRole() {
        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(roleRepository.findByName("ADMIN").get());
        userRepository.save(user);
    }

    private void  createConsumerRole() {
        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail("consumer@gmail.com");
        user.setRole(roleRepository.findByName("CONSUMER").get());
        userRepository.save(user);
    }
}
