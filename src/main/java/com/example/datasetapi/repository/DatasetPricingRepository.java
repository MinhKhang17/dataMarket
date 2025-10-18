package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.DatasetPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetPricingRepository extends JpaRepository<DatasetPricing, Long> {
}
