package com.example.datasetapi.repository;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.dataset.ApiAccessToken;
import com.example.datasetapi.model.userManager.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiAccessTokenRepository extends JpaRepository<ApiAccessToken, UUID> {
    Optional<ApiAccessToken> findByToken(String token);
    Optional<ApiAccessToken> findById(UUID id);

    List<ApiAccessToken> findAllByBuyer(User buyer);
}
