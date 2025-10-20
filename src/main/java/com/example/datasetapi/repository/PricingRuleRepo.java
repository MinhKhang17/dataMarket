package com.example.datasetapi.repository;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.enums.Datasets.SubType;
import com.example.datasetapi.model.dataset.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingRuleRepo extends JpaRepository<PricingRule,Long> {
    PricingRule findByMethodAndDatasetPack(PricingMethod method, DatasetPack datasetPack);

    PricingRule findByMethodAndSubType(PricingMethod method, SubType subType);
}
