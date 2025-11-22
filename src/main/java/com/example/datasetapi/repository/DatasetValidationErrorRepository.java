package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.Dataset;

import com.example.datasetapi.model.dataset.DatasetValidationError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DatasetValidationErrorRepository extends JpaRepository<DatasetValidationError, Long> {
    List<DatasetValidationError> findByDataset(Dataset datasetInformation);
    void deleteByDataset(Dataset datasetInformation);

}
