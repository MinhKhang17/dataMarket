package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.AnalysisMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AnalysisMetricsRepository extends JpaRepository<AnalysisMetrics, Long> {
    @Query("select m from AnalysisMetrics m where m.datasetAnalysis.id = ?1 order by m.createdAt desc")
    AnalysisMetrics findTopByDatasetAnalysisIdOrderByCreatedAtDesc(Long datasetAnalysisId);
}
