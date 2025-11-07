package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.DatasetAnalysis;
import com.example.datasetapi.model.dataset.DatasetInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DatasetAnalysisRepository extends JpaRepository<DatasetAnalysis, Long> {
    Optional<DatasetAnalysis> findByDatasetInformationId(Long datasetInformationId);

    DatasetAnalysis findByDatasetInformation(DatasetInformation datasetInformation);
}
