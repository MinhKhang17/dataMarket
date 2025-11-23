package com.example.datasetapi.dto.response;

import com.example.datasetapi.model.dataset.Dataset;
import jakarta.persistence.Entity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
public class ApiTokenResponse {
    private DatasetDTO dataset;
    private UUID token_id;
    private long userAmount;
    private long userCount;
    private LocalDateTime expiresAt;

}
