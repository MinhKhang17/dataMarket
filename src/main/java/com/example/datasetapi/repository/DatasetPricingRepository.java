package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.DatasetPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatasetPricingRepository extends JpaRepository<DatasetPricing, Long> {

}
