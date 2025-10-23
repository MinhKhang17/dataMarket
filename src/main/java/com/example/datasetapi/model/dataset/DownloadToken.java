package com.example.datasetapi.model.dataset;

import com.example.datasetapi.model.userManager.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "download_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadToken {
    @Id
    private UUID id;

    @ManyToOne
    @NotNull
    private User consumer;

    @ManyToOne
    private Dataset dataset;

    private int use_amount = 5;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime createdAt =LocalDateTime.now();

    @ManyToOne
    private TimeGroup timeGroup;
}
