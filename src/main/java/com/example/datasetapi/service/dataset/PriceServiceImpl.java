package com.example.datasetapi.service.dataset;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.enums.Datasets.SubType;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.dataset.*;
import com.example.datasetapi.repository.DatasetPlanRepo;
import com.example.datasetapi.repository.DatasetPricingRepository;
import com.example.datasetapi.repository.PricingRuleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class PriceServiceImpl implements  PriceService {
    @Autowired
    private PricingRuleRepo pricingRuleRepo;
    @Autowired
    private DatasetPlanRepo datasetPlanRepo;
    @Autowired
    private DatasetPricingRepository datasetPricingRepository;

    @Override
    public void createPricingForDataset(Dataset dataset, DatasetInformation datasetInformation) {
        //tạo giá cho mua một lần
        switch (dataset.getDatasetPack()) {
            case SMALL -> {
                createPricing(dataset,datasetInformation,DatasetPack.SMALL);
            }
            case MEDIUM -> {

                createPricing(dataset,datasetInformation,DatasetPack.MEDIUM);

            }
            case LARGE -> {
                createPricing(dataset,datasetInformation,DatasetPack.LARGE);
            }
            case UNDETERMINED -> {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.DATASET_PACK_INVALID);
            }
        }
    }


    private void createPricing(Dataset dataset, DatasetInformation datasetInformation, DatasetPack datasetPack) {
        Set<DatasetPlan> datasetPlans = new HashSet<>() ;

        // ONE_TIME
        DatasetPlan oneTimePlan = new DatasetPlan();
        oneTimePlan.setPricingMethod(PricingMethod.ONE_TIME);
        oneTimePlan.setDataset(dataset);
        oneTimePlan.setDatasetPack(datasetPack);
        oneTimePlan.setDatasetPricingList(calculatePricing(PricingMethod.ONE_TIME, datasetPack, datasetInformation.getRowCount(), oneTimePlan));
        datasetPlans.add(oneTimePlan);

        // SUBSCRIPTION
        DatasetPlan subTypePlan = new DatasetPlan();
        subTypePlan.setPricingMethod(PricingMethod.SUBSCRIPTION);
        subTypePlan.setDataset(dataset);
        subTypePlan.setDatasetPack(datasetPack);
        subTypePlan.setDatasetPricingList(calculatePricing(PricingMethod.SUBSCRIPTION, datasetPack, datasetInformation.getRowCount(), subTypePlan));
        datasetPlans.add(subTypePlan);

        // API
        DatasetPlan apiPlan = new DatasetPlan();
        apiPlan.setDataset(dataset);
        apiPlan.setPricingMethod(PricingMethod.API);
        apiPlan.setDatasetPack(datasetPack);
        apiPlan.setDatasetPricingList(calculatePricing(PricingMethod.API, datasetPack, datasetInformation.getRowCount(), apiPlan));
        datasetPlans.add(apiPlan);

        // 👉 Lưu DatasetPlan sẽ tự cascade xuống Pricing
        datasetPlanRepo.saveAll(datasetPlans);
    }


    private Set<DatasetPricing> calculatePricing(PricingMethod pricingMethod, DatasetPack datasetPack, Long rowCount, DatasetPlan plan) {
        Set<DatasetPricing> list = new HashSet<>();
        if(pricingMethod == PricingMethod.ONE_TIME) {
            DatasetPricing datasetPricing = new DatasetPricing();
            datasetPricing.setPricingRule(pricingRuleRepo.findByMethodAndDatasetPack(pricingMethod, datasetPack));
            datasetPricing.setPrice(oneTimePricingCal(datasetPack,rowCount));
            datasetPricing.setDatasetPack(datasetPack);
            datasetPricing.setPricingMethod(PricingMethod.ONE_TIME);
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
           PricingRule pricingRule = pricingRuleRepo.findByMethodAndSubType(pricingMethod, SubType.SMALL);
           DatasetPricing smallApi = new DatasetPricing();
           smallApi.setPricingRule(pricingRule);
           smallApi.setPricePerRequest(apiPricingCal(pricingRule));
           smallApi.setDatasetPack(DatasetPack.SMALL);
           smallApi.setPricingMethod(pricingMethod);
           list.add(smallApi);

           DatasetPricing mediumApi = new DatasetPricing();
           pricingRule = pricingRuleRepo.findByMethodAndSubType(pricingMethod, SubType.MEDIUM);
           mediumApi.setPricingRule(pricingRule);
           mediumApi.setPricingMethod(pricingMethod);
           mediumApi.setPricePerRequest(apiPricingCal(pricingRule));
           mediumApi.setDatasetPack(DatasetPack.MEDIUM);
           list.add(mediumApi);

           DatasetPricing largeApi = new DatasetPricing();
           pricingRule= pricingRuleRepo.findByMethodAndSubType(pricingMethod, SubType.LARGE);
           largeApi.setPricingRule(pricingRule);
           largeApi.setPricingMethod(pricingMethod);
           largeApi.setPricePerRequest(apiPricingCal(pricingRule));
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
