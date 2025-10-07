package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.DatasetGroup;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.model.userManager.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetGroupRepository extends JpaRepository<DatasetGroup,Long> {
    DatasetGroup findByAddressAndDatasetType(Address address, DatasetType datasetType);
}
