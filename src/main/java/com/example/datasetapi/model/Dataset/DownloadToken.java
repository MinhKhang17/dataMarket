package com.example.datasetapi.model.dataset;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "download_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadToken {
    @Id
    private UUID id;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false)
    private long datasetId;

    private boolean used = false;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant createdAt =Instant.now();
}
