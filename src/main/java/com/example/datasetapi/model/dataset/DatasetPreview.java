package com.example.datasetapi.model.dataset;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "dataset_preview")
public class DatasetPreview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false, unique = true)
    private Dataset dataset;

    // LƯU JSON STRING BÌNH THƯỜNG
    @Column(name = "headers", columnDefinition = "text")
    private String headersJson;

    @Column(name = "rows", columnDefinition = "text")
    private String rowsJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
