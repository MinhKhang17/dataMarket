package com.example.datasetapi.repository;

import com.example.datasetapi.enums.Datasets.ValidationPhase;
import com.example.datasetapi.model.Dataset.DatasetError;
import com.example.datasetapi.model.Dataset.DatasetModeration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatasetErrorRepository extends JpaRepository<DatasetError, Long> {
    List<DatasetError> findByDatasetIdAndPhase(Long datasetId, ValidationPhase phase);
}

