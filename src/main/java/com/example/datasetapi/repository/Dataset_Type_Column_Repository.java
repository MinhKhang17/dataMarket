package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.DatasetTypeColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Dataset_Type_Column_Repository extends JpaRepository<DatasetTypeColumn,Long> {
    List<DatasetTypeColumn> findByColumnNameIn(List<String> list);

    boolean existsByColumnName(String name);
}
