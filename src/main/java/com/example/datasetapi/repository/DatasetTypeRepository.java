package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.DatasetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetTypeRepository  extends JpaRepository<DatasetType, Long> {

    DatasetType findByName(String evStationLocationBasic);
}
