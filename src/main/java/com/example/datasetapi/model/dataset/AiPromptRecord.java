package com.example.datasetapi.model.dataset;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "ai_prompt")
@Data
public class AiPromptRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_analysis_id", nullable = false)
    private DatasetAnalysis datasetAnalysis;

    @Column(name = "prompt_text", columnDefinition = "text")
    private String promptText;

    @Column(name = "ai_response_json", columnDefinition = "text")
    private String aiResponseJson;

    @Column(name = "sample_csv", columnDefinition = "text")
    private String sampleCsv;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    public AiPromptRecord() { this.createdAt = Instant.now(); }

    }
