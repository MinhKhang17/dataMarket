package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.dataset.DatasetPlan;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import io.micrometer.common.KeyValues;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface DatasetPlanRepo extends JpaRepository<DatasetPlan, Integer> {
    KeyValues findByDataset(Dataset dataset);

    List<DatasetPlan> findALlByDataset(Dataset dataset);

    Set<DatasetPlan> findALlByDatasetId(Long datasetId);

    DatasetPlan findByDatasetAndPricingMethod(Dataset dataset, PricingMethod pricingMethod);
}
