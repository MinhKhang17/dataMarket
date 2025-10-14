package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.DatasetGroup;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.model.UserManager.Provider;
import com.example.datasetapi.model.location.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DatasetGroupRepository extends JpaRepository<DatasetGroup,Long> {

    Optional<DatasetGroup> findByProviderAndProvinceAndDatasetType(Provider provider, Province province, DatasetType datasetType);
}
