package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface ProviderService {
    ResponseEntity<ApiResponse> viewProfile();
}
