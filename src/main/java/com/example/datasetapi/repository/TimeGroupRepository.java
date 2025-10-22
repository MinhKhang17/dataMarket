package com.example.datasetapi.repository;

import com.example.datasetapi.dto.response.TimeGroupDTO;
import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetGroup;
import com.example.datasetapi.model.Dataset.TimeGroup;
import com.example.datasetapi.model.UserManager.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeGroupRepository extends JpaRepository<TimeGroup, Long> {



    List<TimeGroup> findAllByDatasetGroupChild(DatasetGroup datasetGroupChild);

    List<TimeGroup> findAllByDatasetGroupChildId(long datasetGroupChildId);

    Optional<TimeGroup> findByYearAndMonthAndDatasetGroupChildAndProvider(int year, int monthValue, DatasetGroup childGroup, Provider provider);
}

