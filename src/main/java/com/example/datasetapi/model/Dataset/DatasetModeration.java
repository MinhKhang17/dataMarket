package com.example.datasetapi.model.Dataset;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "dataset_moderation")
@Getter @Setter
public class DatasetModeration {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "dataset_id")
    private DatasetInfor dataset;

    @Column private Double errorRate; // % tổng lỗi/(rows*columns) hoặc % lỗi bản ghi tùy cách tính
    @Column private Long totalRows;
    @Column private Instant createdAt = Instant.now();

    @Column private Boolean autoSuggestedApprove; // true nếu <=2%
    @Column(length = 1000) private String note;   // ghi chú cho moderator
}
