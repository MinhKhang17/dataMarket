package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.enums.Datasets.PricingType;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.Dataset.*;
import com.example.datasetapi.repository.DatasetPlanRepo;
import com.example.datasetapi.repository.PricingRuleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PriceServiceImpl implements  PriceService {
    @Autowired
    private PricingRuleRepo pricingRuleRepo;
    @Autowired
    private DatasetPlanRepo datasetPlanRepo;
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
                throw new CustomException(ErrorCode.DATASET_PACK_INVALID);
            }
        }
    }

    private void createPricing(Dataset dataset, DatasetInformation datasetInformation,DatasetPack datasetPack) {
            DatasetPlan oneTimePlan = new DatasetPlan();
            oneTimePlan.setDataset(dataset);
            oneTimePlan.setDatasetPack(datasetPack);
            List<DatasetPricing> listOneTime = new ArrayList<>();
            listOneTime = calculatePricing(PricingMethod.ONE_TIME,datasetPack,datasetInformation.getRowCount());

    }

    private List<DatasetPricing> calculatePricing(PricingMethod pricingMethod, DatasetPack datasetPack, Long rowCount) {
        if(pricingMethod == PricingMethod.ONE_TIME) {
            DatasetPricing datasetPricing = new DatasetPricing();
            datasetPricing.setPricingRule(pricingRuleRepo.findByMethodAndDatasetPack(pricingMethod, datasetPack));
            datasetPricing.setPrice();
        }
    }


}
