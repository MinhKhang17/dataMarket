package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.DatasetGroup;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.model.location.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetGroupRepository extends JpaRepository<DatasetGroup,Long> {
    DatasetGroup findByLocationAndDatasetType(Location Location, DatasetType datasetType);
}
