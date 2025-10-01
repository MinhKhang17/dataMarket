package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<Dataset, Long> {}
