package com.example.datasetapi.repository;


import com.example.datasetapi.model.dataset.DatasetGroup;
import com.example.datasetapi.model.dataset.TimeGroup;
import com.example.datasetapi.model.userManager.Provider;
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

