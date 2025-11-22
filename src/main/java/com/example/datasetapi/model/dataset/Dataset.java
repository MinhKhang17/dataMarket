package com.example.datasetapi.model.dataset;

import com.example.datasetapi.dto.response.ValidationErrorDto;
import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.enums.Datasets.FileExtension;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
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

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DatasetStatus datasetStatus = DatasetStatus.PENDING;

    @Column
    private String fileKey;

    @Column
    private String fileUrl;

    @Column
    @Enumerated(EnumType.STRING)
    private FileExtension datasetExtension;


    @Column
    private Long rowCount;

    @Column
    private Long downloadCount = 0L;

    @Column
    private boolean isHeaderChecked = false;

    @Column
    private boolean isContentChecked = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column
    private LocalDate datasetTime;

    @Column
    @Enumerated(EnumType.STRING)
    private DatasetPack datasetPack = DatasetPack.UNDETERMINED;

    @Column
    @Enumerated(EnumType.STRING)
    private DatasetSourceType datasetSourceType;

    // ==================== TRANSIENT FIELDS ====================

    @Transient
    private List<ValidationErrorDto> validationErrors;

    // ==================== RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_group_id")
    @JsonIgnore
    private DatasetGroup datasetChildGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_type_id")
    @JsonIgnore
    private DatasetType datasetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    @JsonIgnore
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    @JsonIgnore
    private User moderator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    @JsonIgnore
    private Commune commune;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_group_id")
    @JsonIgnore
    private TimeGroup timeGroup;

    @OneToMany(mappedBy = "dataset", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DatasetPlan> datasetPlans = new ArrayList<>();

    @OneToMany(mappedBy = "dataset", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DatasetValidationError> datasetValidationErrors = new ArrayList<>();

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
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.datasetStatus == null) {
            this.datasetStatus = DatasetStatus.PENDING;
        }
        if (this.datasetPack == null) {
            this.datasetPack = DatasetPack.UNDETERMINED;
        }
        if (this.downloadCount == null) {
            this.downloadCount = 0L;
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

    /**
     * Thêm DatasetValidationError vào Dataset
     */
    public void addValidationError(DatasetValidationError error) {
        datasetValidationErrors.add(error);
        error.setDataset(this);
    }

    /**
     * Xóa DatasetValidationError khỏi Dataset
     */
    public void removeValidationError(DatasetValidationError error) {
        datasetValidationErrors.remove(error);
        error.setDataset(null);
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
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", datasetStatus=" + datasetStatus +
                ", datasetPack=" + datasetPack +
                ", rowCount=" + rowCount +
                '}';
    }
}