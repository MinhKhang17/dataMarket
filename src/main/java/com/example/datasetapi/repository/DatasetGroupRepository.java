package com.example.datasetapi.repository;

import com.example.datasetapi.enums.Datasets.DatasetGroupType;
import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.model.dataset.DatasetGroup;
import com.example.datasetapi.model.dataset.DatasetType;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.location.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface DatasetGroupRepository extends JpaRepository<DatasetGroup,Long> {




    List<DatasetGroup> findByDatasetGroupType(DatasetGroupType datasetGroupType);

    Optional<DatasetGroup> findByIdAndDatasetGroupType(long id, DatasetGroupType datasetGroupType);

    Optional<DatasetGroup> findByDatasetGroupTypeAndProvinceAndDatasetType(DatasetGroupType datasetGroupType, Province province, DatasetType datasetType);

    List<DatasetGroup> findByDatasetGroupTypeAndDatasetSourceType(DatasetGroupType datasetGroupType, DatasetSourceType datasetSourceType);

    Optional<DatasetGroup> findByIdAndDatasetGroupTypeAndDatasetSourceType(long id, DatasetGroupType datasetGroupType, DatasetSourceType datasetSourceType);

    List<DatasetGroup> findAllByDatasetGroupTypeAndDatasetSourceType(DatasetGroupType datasetGroupType, DatasetSourceType datasetSourceType);

    Optional<DatasetGroup> findByDatasetGroupTypeAndProvinceAndDatasetTypeAndDatasetSourceType(DatasetGroupType datasetGroupType, Province province, DatasetType datasetType, DatasetSourceType datasetSourceType);


        @Query("SELECT dg FROM DatasetGroup dg " +
                "WHERE dg.parent = :parent " +
                "AND dg.commune = :commune " +
                "AND dg.datasetSourceType = :sourceType")
        Optional<DatasetGroup> findByParentAndCommuneAndDatasetSourceType(
                @Param("parent") DatasetGroup parent,
                @Param("commune") Commune commune,
                @Param("sourceType") DatasetSourceType sourceType
        );
    }
