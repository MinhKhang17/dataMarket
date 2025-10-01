package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.DatasetInfor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<DatasetInfor, Long> {}
