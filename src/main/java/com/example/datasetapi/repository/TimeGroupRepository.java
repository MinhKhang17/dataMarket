package com.example.datasetapi.repository;

import com.example.datasetapi.dto.response.TimeGroupDTO;
import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetGroup;
import com.example.datasetapi.model.Dataset.TimeGroup;
import com.example.datasetapi.model.UserManager.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimeGroupRepository extends JpaRepository<TimeGroup, Long> {
    Optional<TimeGroup> findByYearAndMonthAndDayAndDatasetGroupChildAndProvider(
            int year, int month, int day, DatasetGroup datasetGroupChild, Provider provider
    );


    List<TimeGroup> findAllByDatasetGroupChild(DatasetGroup datasetGroupChild);

    List<TimeGroup> findAllByDatasetGroupChildId(long datasetGroupChildId);

}

