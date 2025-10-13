package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.ReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewHistoryRepository extends JpaRepository<ReviewHistory, Long> {
}
