package com.example.datasetapi.repository;

import com.example.datasetapi.model.UserManager.ProviderRegisReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRegisReviewHistoryRepo extends JpaRepository<ProviderRegisReviewHistory,Long> {
}
