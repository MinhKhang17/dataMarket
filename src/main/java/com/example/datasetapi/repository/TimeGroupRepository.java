package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.DatasetGroup;
import com.example.datasetapi.model.dataset.TimeGroup;
import com.example.datasetapi.model.userManager.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimeGroupRepository extends JpaRepository<TimeGroup, Long> {
    Optional<TimeGroup> findByYearAndMonthAndDayAndDatasetGroupChildAndProvider(
            int year, int month, int day, DatasetGroup datasetGroupChild, Provider provider
    );
}

