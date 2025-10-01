package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.DatasetInfor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetInfRepository extends JpaRepository<DatasetInfor, Long> {}
