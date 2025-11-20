package com.example.datasetapi.service.analytics;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.RevenueResponse;
import org.springframework.http.ResponseEntity;

public interface AnalyticsService {
    RevenueResponse getRevenue();
}
