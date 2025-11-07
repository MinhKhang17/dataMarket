package com.example.datasetapi.repository;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.dataset.ProviderRevenue;
import com.example.datasetapi.model.userManager.Provider;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProviderRevenueRepo extends JpaRepository<ProviderRevenue, String> {


    List<ProviderRevenue> findAllByProviderAndCreatedAtAfterOrderByCreatedAtDesc(
            Provider provider,
            LocalDateTime after);}
