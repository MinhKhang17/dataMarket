package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.DatasetPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetPlanRepo extends JpaRepository<DatasetPlan, Integer> {
}
