package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetPlan;
import io.micrometer.common.KeyValues;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface DatasetPlanRepo extends JpaRepository<DatasetPlan, Integer> {
    KeyValues findByDataset(Dataset dataset);

    List<DatasetPlan> findALlByDataset(Dataset dataset);

    Set<DatasetPlan> findALlByDatasetId(Long datasetId);
}
