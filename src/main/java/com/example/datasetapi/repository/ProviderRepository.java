package com.example.datasetapi.repository;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProviderRepository extends JpaRepository<Provider,Long> {
    Provider findByUserId(Long userId);

}
