package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.DatasetOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<DatasetOrder,Long> {
    List<DatasetOrder> findByUserId(Long userId);

}
