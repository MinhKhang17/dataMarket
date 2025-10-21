package com.example.datasetapi.repository;

import com.example.datasetapi.dto.response.DatasetParentReposonseDto;
import com.example.datasetapi.enums.Datasets.DatasetGroupType;
import com.example.datasetapi.model.Dataset.DatasetGroup;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.model.UserManager.Provider;
import com.example.datasetapi.model.location.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DatasetGroupRepository extends JpaRepository<DatasetGroup,Long> {




    List<DatasetGroup> findByDatasetGroupType(DatasetGroupType datasetGroupType);

    Optional<DatasetGroup> findByIdAndDatasetGroupType(long id, DatasetGroupType datasetGroupType);

    Optional<DatasetGroup> findByDatasetGroupTypeAndProvinceAndDatasetType(DatasetGroupType datasetGroupType, Province province, DatasetType datasetType);
}
