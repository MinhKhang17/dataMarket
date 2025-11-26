package com.example.datasetapi.repository;


import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.dataset.DatasetPreview;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatasetPreviewRepository extends JpaRepository<DatasetPreview, Integer> {
    Optional<DatasetPreview> findByDataset(Dataset dataset);
}
