package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.DatasetInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetInforRepository extends JpaRepository<DatasetInformation, Long> {}
