package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.ApiAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiAccessTokenRepository extends JpaRepository<ApiAccessToken, UUID> {
    Optional<ApiAccessToken> findByToken(String token);
    Optional<ApiAccessToken> findById(UUID id);
}
