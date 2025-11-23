package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.enums.Datasets.*;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.mapper.DatasetMapper;
import com.example.datasetapi.dto.service.PricingRuleDTO;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.dataset.*;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.repository.DatasetPlanRepo;
import com.example.datasetapi.repository.DatasetPricingRepository;
import com.example.datasetapi.repository.PricingRuleRepo;
import com.example.datasetapi.repository.ProviderRevenueRepo;
import com.example.datasetapi.service.payment.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PriceServiceImpl implements  PriceService {
    @Autowired
    private PricingRuleRepo pricingRuleRepo;
    @Autowired
    private DatasetPlanRepo datasetPlanRepo;
    @Autowired
    private DatasetPricingRepository datasetPricingRepository;
    @Autowired
    private DatasetMapper datasetMapper;
    @Autowired
    private PaymentService paymentService ;
@Autowired
private ProviderRevenueRepo providerRevenueRepo;
    @Override
    public void createPricingForDataset(Dataset dataset,  ProviderUploadDatasetRequest request) {
        //tạo giá cho mua một lần
        switch (dataset.getDatasetPack()) {
            case SMALL -> {
                createPricing(dataset,DatasetPack.SMALL,request);
            }
            case MEDIUM -> {

                createPricing(dataset,DatasetPack.MEDIUM, request);

            }
            case LARGE -> {
                createPricing(dataset,DatasetPack.LARGE, request);
            }
            case UNDETERMINED -> {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.DATASET_PACK_INVALID);
            }
        }
    }

    @Override
    public PricingRule findSubPricingRuleById(long pricingSubRuleId) {
        return pricingRuleRepo.findByMethodAndId(PricingMethod.SUBSCRIPTION,pricingSubRuleId).orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND,ErrorCode.RuleNotFound));
    }

    @Override
    public List<PricingRuleDTO> getAllSubPack() {
        return pricingRuleRepo.findAllByMethod(PricingMethod.SUBSCRIPTION)
                .stream()
                .map(datasetMapper::toPricingRuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingRuleDTO> getAllAPIPricingRule() {
        return pricingRuleRepo.findAllByMethod(PricingMethod.API)
                .stream()
                .map(datasetMapper::toPricingRuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PricingRule findApiPricingRuleById(long apiPackId) {
        return pricingRuleRepo.findByIdAndMethod(apiPackId,PricingMethod.API);
    }

    @Override
    public void createRevenueForProvider(Provider provider, DatasetPack datasetPack, Dataset dataset) {
        // tìm lợi nhuận cho provider đã được định nghĩa
        PricingRule pricingRule =  pricingRuleRepo.findByMethodAndDatasetPack(PricingMethod.PROVIDER_REVENUE,datasetPack);

        double basePricePoint = pricingRule.getBasePricePoint();

        createProviderRevenue(provider,basePricePoint,dataset);

        paymentService.updateWallet(TransferType.TOUP,basePricePoint,provider.getUser().getId(),BuyType.PROVIDER_REVENUE);
    }

    private void createProviderRevenue(Provider provider, double point, Dataset dataset) {
        ProviderRevenue providerRevenue = new ProviderRevenue();
        providerRevenue.setProvider(provider);
        providerRevenue.setRevenue_amount(point);
        providerRevenue.setDataset(dataset);
        providerRevenueRepo.save(providerRevenue);
    }


    private void createPricing(Dataset dataset, DatasetPack datasetPack, ProviderUploadDatasetRequest request) {
        Set<DatasetPlan> datasetPlans = new HashSet<>();

        // ONE_TIME
        DatasetPlan oneTimePlan = new DatasetPlan();
        oneTimePlan.setPricingMethod(PricingMethod.ONE_TIME);
        oneTimePlan.setDataset(dataset);
        oneTimePlan.setDatasetPack(datasetPack);

        Set<DatasetPricing> oneTimePricings = calculatePricing(PricingMethod.ONE_TIME, datasetPack, dataset.getRowCount(), oneTimePlan, dataset, request);

        // ✅ Set quan hệ 2 chiều
        for (DatasetPricing pricing : oneTimePricings) {
            pricing.setDatasetPlan(oneTimePlan);
        }
        oneTimePlan.setDatasetPricingList(oneTimePricings);

        datasetPlans.add(oneTimePlan);

        // SUBSCRIPTION
        DatasetPlan subTypePlan = new DatasetPlan();
        subTypePlan.setPricingMethod(PricingMethod.SUBSCRIPTION);
        subTypePlan.setDataset(dataset);
        subTypePlan.setDatasetPack(datasetPack);

        Set<DatasetPricing> subPricings = calculatePricing(PricingMethod.SUBSCRIPTION, datasetPack, dataset.getRowCount(), subTypePlan, dataset, request);

//        // ✅ Set quan hệ 2 chiều
//        for (DatasetPricing pricing : subPricings) {
//            pricing.setDatasetPlan(subTypePlan);
//        }
//        subTypePlan.setDatasetPricingList(subPricings);
//
//        datasetPlans.add(subTypePlan);
//
        DatasetPlan apiTypePlan = new DatasetPlan();
        apiTypePlan.setPricingMethod(PricingMethod.API);
        apiTypePlan.setDataset(dataset);
        apiTypePlan.setDatasetPack(datasetPack);

        Set<DatasetPricing> apiPricing = calculatePricing(PricingMethod.API, datasetPack, dataset.getRowCount(), apiTypePlan, dataset, request);
        for (DatasetPricing pricing : apiPricing) {
            pricing.setDatasetPlan(apiTypePlan);
        }
        apiTypePlan.setDatasetPricingList(apiPricing);
        datasetPlans.add(apiTypePlan);

        // ✅ Save all
        datasetPlanRepo.saveAll(datasetPlans);
    }

    private Set<DatasetPricing> calculatePricing(PricingMethod pricingMethod, DatasetPack datasetPack, Long rowCount, DatasetPlan plan, Dataset dataset, ProviderUploadDatasetRequest request) {
        Set<DatasetPricing> list = new HashSet<>();
        if(pricingMethod == PricingMethod.ONE_TIME) {
            DatasetPricing datasetPricing = new DatasetPricing();
            datasetPricing.setPricingRule(pricingRuleRepo.findByMethodAndDatasetPack(pricingMethod, datasetPack));
            datasetPricing.setDatasetPack(datasetPack);
            datasetPricing.setPricingMethod(PricingMethod.ONE_TIME);
            if(request.getPrice()!=0.0){
                datasetPricing.setPrice(request.getPrice());
            }
            else{
                datasetPricing.setPrice(oneTimePricingCal(datasetPack,rowCount));
            }
//            dataset.getTimeGroup().setPrice(dataset.getTimeGroup().getPrice()+datasetPricing.getPrice());
            list.add(datasetPricing);
            return list;
        }
       else if(pricingMethod == PricingMethod.SUBSCRIPTION){
           PricingRule pricingRule = pricingRuleRepo.findByMethodAndSubType(pricingMethod, SubType.SMALL);
           DatasetPricing smallPackPrice = new DatasetPricing();
            smallPackPrice.setPricingRule(pricingRule);
            smallPackPrice.setPrice(subPricingCal(rowCount,pricingRule));
           smallPackPrice.setDatasetPack(DatasetPack.SMALL);
            smallPackPrice.setPricingMethod(pricingMethod);
            list.add(smallPackPrice);
           DatasetPricing mediumPackPrice = new DatasetPricing();
           pricingRule = pricingRuleRepo.findByMethodAndSubType(pricingMethod, SubType.MEDIUM);
           mediumPackPrice.setPricingRule(pricingRule);
           mediumPackPrice.setPrice(subPricingCal(rowCount,pricingRule));
           mediumPackPrice.setDatasetPack(DatasetPack.MEDIUM);
           mediumPackPrice.setPricingMethod(pricingMethod);
           list.add(mediumPackPrice);
           DatasetPricing largePackPrice = new DatasetPricing();
           pricingRule = pricingRuleRepo.findByMethodAndSubType(pricingMethod, SubType.LARGE);
           largePackPrice.setPricingRule(pricingRule);
           largePackPrice.setPrice(subPricingCal(rowCount,pricingRule));
           largePackPrice.setDatasetPack(DatasetPack.LARGE);
           largePackPrice.setPricingMethod(pricingMethod);



           list.add(largePackPrice);
           return list;
       }
       else {

               PricingRule pricingRule = pricingRuleRepo.findByMethodAndDatasetPack(pricingMethod, DatasetPack.SMALL);
               DatasetPricing smallApi = new DatasetPricing();
               smallApi.setPricingRule(pricingRule);
               smallApi.setPrice(apiPricingCal(pricingRule));
               smallApi.setDatasetPack(DatasetPack.SMALL);
               smallApi.setPricingMethod(pricingMethod);
               list.add(smallApi);


               DatasetPricing mediumApi = new DatasetPricing();
               pricingRule = pricingRuleRepo.findByMethodAndDatasetPack(pricingMethod, DatasetPack.MEDIUM);
               mediumApi.setPricingRule(pricingRule);
               mediumApi.setPricingMethod(pricingMethod);
               mediumApi.setPrice(apiPricingCal(pricingRule));
               mediumApi.setDatasetPack(DatasetPack.MEDIUM);
               list.add(mediumApi);



               DatasetPricing largeApi = new DatasetPricing();
               pricingRule = pricingRuleRepo.findByMethodAndDatasetPack(pricingMethod, DatasetPack.LARGE);
               largeApi.setPricingRule(pricingRule);
               largeApi.setPricingMethod(pricingMethod);
               largeApi.setPrice(apiPricingCal(pricingRule));
               largeApi.setDatasetPack(DatasetPack.LARGE);
               list.add(largeApi);

           return list;
        }
    }

    private double apiPricingCal(PricingRule pricingRule) {
        return pricingRule.getBasePricePoint() / pricingRule.getRequestLimit();
    }

    private double subPricingCal(Long rowCount, PricingRule pricingRule) {
        double totalOfPackage = pricingRule.getBasePricePoint();
        long totalRowInpackage = pricingRule.getRowLimit();
        double result = (totalOfPackage / totalRowInpackage) * rowCount;
        return result;
    }

    private double oneTimePricingCal(DatasetPack datasetPack, Long rowCount) {
        PricingRule oneTimePricingRule = pricingRuleRepo.findByMethodAndDatasetPack(PricingMethod.ONE_TIME,datasetPack);
        return rowCount * oneTimePricingRule.getBasePricePerRowPoint();
    }


}
