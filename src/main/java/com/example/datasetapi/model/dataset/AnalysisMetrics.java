package com.example.datasetapi.model.dataset;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "analysis_metrics",
        indexes = { @Index(name = "idx_metrics_dataset_analysis_id", columnList = "dataset_analysis_id") })
public class AnalysisMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_analysis_id", nullable = false)
    private DatasetAnalysis datasetAnalysis;

    @Column(name = "type", length = 100)
    private String type;

    // store raw JSON as text/jsonb in DB (configure columnDefinition on DB side if needed)
    @Column(name = "metrics_json", columnDefinition = "text")
    private String metricsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AnalysisMetrics() { this.createdAt = Instant.now(); }

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DatasetAnalysis getDatasetAnalysis() { return datasetAnalysis; }
    public void setDatasetAnalysis(DatasetAnalysis datasetAnalysis) { this.datasetAnalysis = datasetAnalysis; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMetricsJson() { return metricsJson; }
    public void setMetricsJson(String metricsJson) { this.metricsJson = metricsJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
