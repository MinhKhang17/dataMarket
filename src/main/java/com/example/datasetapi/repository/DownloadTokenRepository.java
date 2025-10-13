package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.DownloadToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface DownloadTokenRepository extends JpaRepository<DownloadToken, UUID> {
    DownloadToken findDownloadTokenById(UUID id);
}
