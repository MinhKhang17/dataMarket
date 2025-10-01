package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.DownloadToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface DowloadTokenRepository extends JpaRepository<DownloadToken, UUID> {
    DownloadToken findDownloadTokenById(UUID id);
}
