package com.example.datasetapi.model.dataset;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dataset")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    @Enumerated(EnumType.STRING)
    private DatasetStatus datasetStatus = DatasetStatus.PENDING;

    @Column
    private String fileKey;

    @Column
    private Integer version;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column
    @Enumerated(EnumType.STRING)
    private DatasetPack datasetPack = DatasetPack.UNDETERMINED;

    @Column
    private Long row_count;

    @Column
    private Long dowload_count = 0L;

    @Column
    @Enumerated(EnumType.STRING)
    private DatasetSourceType datasetSourceType;

    // ==================== RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_group_id")
    @JsonIgnore
    private DatasetGroup datasetChildGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    @JsonIgnore
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    @JsonIgnore
    private User moderator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_group_id")
    @JsonIgnore
    private TimeGroup timeGroup;

    @OneToMany(mappedBy = "dataset", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DatasetPlan> datasetPlans = new ArrayList<>();

    // ==================== LIFECYCLE CALLBACKS ====================

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.datasetStatus == null) {
            this.datasetStatus = DatasetStatus.PENDING;
        }
        if (this.datasetPack == null) {
            this.datasetPack = DatasetPack.UNDETERMINED;
        }
        if (this.dowload_count == null) {
            this.dowload_count = 0L;
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Thêm DatasetPlan vào Dataset
     */
    public void addDatasetPlan(DatasetPlan plan) {
        datasetPlans.add(plan);
        plan.setDataset(this);
    }

    /**
     * Xóa DatasetPlan khỏi Dataset
     */
    public void removeDatasetPlan(DatasetPlan plan) {
        datasetPlans.remove(plan);
        plan.setDataset(null);
    }

    // ==================== EQUALS & HASHCODE ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dataset)) return false;
        Dataset dataset = (Dataset) o;
        return id != null && id.equals(dataset.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // ==================== TOSTRING ====================

    @Override
    public String toString() {
        return "Dataset{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", version=" + version +
                ", datasetStatus=" + datasetStatus +
                ", datasetPack=" + datasetPack +
                '}';
    }
}