package com.example.datasetapi.repository;

import com.example.datasetapi.dto.response.DatasetDTO;
import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.dataset.DownloadToken;
import com.example.datasetapi.model.userManager.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DownloadTokenRepository extends JpaRepository<DownloadToken, UUID> {
    DownloadToken findDownloadTokenById(UUID id);

    Optional<DownloadToken> findByConsumerAndDataset(User consumer, Dataset dataset);

    List<DownloadToken> findByConsumer(User consumer);
}
