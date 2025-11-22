package com.example.datasetapi.model.dataset;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasetAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK id tới DatasetInformation (giữ song song để query nhanh mà không cần join)
     */
    @Column(name = "dataset_information_id", nullable = false, unique = true)
    private Long datasetInformationId;

    /**
     * Optional: ManyToOne relation tới DatasetInformation.
     * Nếu bạn muốn tránh join, bạn có thể comment phần @ManyToOne này.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", insertable = false, updatable = false)
    private Dataset datasetInformation;

    @Column(name = "dataset_type", length = 150)
    private String datasetType;

    @Column(name = "status", length = 100)
    private String status;

    @Column(name = "row_count")
    private Long rowCount;

    @Column(name = "error_rate_percent")
    private Double errorRatePercent;

    @Column(name = "total_errors")
    private Long totalErrors;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
