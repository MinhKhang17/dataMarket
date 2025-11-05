package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.AiPromptRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AiPromptRecordRepository extends JpaRepository<AiPromptRecord, Long> {
    @Query("select p from AiPromptRecord p where p.datasetAnalysis.id = ?1 order by p.createdAt desc")
    AiPromptRecord findTopByDatasetAnalysisIdOrderByCreatedAtDesc(Long datasetAnalysisId);
}
