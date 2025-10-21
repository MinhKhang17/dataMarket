package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.DownloadToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DownloadTokenRepository extends JpaRepository<DownloadToken, UUID> {
    DownloadToken findDownloadTokenById(UUID id);
}
