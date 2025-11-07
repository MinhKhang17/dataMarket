package com.example.datasetapi.repository;

import com.example.datasetapi.enums.Datasets.DatasetInforStatus;
import com.example.datasetapi.model.dataset.DatasetInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatasetInforRepository extends JpaRepository<DatasetInformation, Long> {
    List<DatasetInformation> findAllByStatus(DatasetInforStatus status);

    DatasetInformation findByDatasetId(Long datasetId);
}
