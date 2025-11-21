package com.example.datasetapi.model.dataset;

import com.example.datasetapi.enums.Datasets.DatasetGroupType;
import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dataset_group")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DatasetGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer version = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DatasetGroupType datasetGroupType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DatasetSourceType datasetSourceType;

    // ==================== RELATIONSHIPS ====================

    // DatasetType (nhiều DatasetGroup -> 1 DatasetType)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_type_id")
    @JsonIgnore
    @ToString.Exclude
    private DatasetType datasetType;

    // Provider (nhiều DatasetGroup -> 1 Provider)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    @JsonIgnore
    @ToString.Exclude
    private Provider provider;

    // Province (chỉ có ở parent group)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    @JsonIgnore
    @ToString.Exclude
    private Province province;

    // Commune (chỉ có ở child group)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    @JsonIgnore
    @ToString.Exclude
    private Commune commune;

    // ==================== BIDIRECTIONAL RELATIONSHIPS ====================

    // Parent-Child relationship (Self-referencing)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DatasetGroup parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DatasetGroup> datasetGroups = new ArrayList<>();

    // Datasets (1 DatasetGroup -> nhiều Dataset)
    // ⚠️ KHÔNG dùng cascade ở đây để tránh orphanRemoval conflict
    @OneToMany(mappedBy = "datasetChildGroup", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Dataset> datasets = new ArrayList<>();

    // TimeGroups (1 DatasetGroup -> nhiều TimeGroup)
    @OneToMany(mappedBy = "datasetGroupChild", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TimeGroup> timeGroups = new ArrayList<>();

    // ==================== METADATA ====================

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updateAt = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean isHaveData = false;

    // ==================== HELPER METHODS ====================

    /**
     * Thêm child group vào parent group
     */
    public void addChildGroup(DatasetGroup child) {
        if (child != null) {
            this.datasetGroups.add(child);
            child.setParent(this);
        }
    }

    /**
     * Xóa child group khỏi parent group
     */
    public void removeChildGroup(DatasetGroup child) {
        if (child != null) {
            this.datasetGroups.remove(child);
            child.setParent(null);
        }
    }

    /**
     * Thêm dataset vào group
     */
    public void addDataset(Dataset dataset) {
        if (dataset != null) {
            this.datasets.add(dataset);
            dataset.setDatasetChildGroup(this);
        }
    }

    /**
     * Xóa dataset khỏi group
     */
    public void removeDataset(Dataset dataset) {
        if (dataset != null) {
            this.datasets.remove(dataset);
            dataset.setDatasetChildGroup(null);
        }
    }

    /**
     * Thêm time group
     */
    public void addTimeGroup(TimeGroup timeGroup) {
        if (timeGroup != null) {
            this.timeGroups.add(timeGroup);
            timeGroup.setDatasetGroupChild(this);
        }
    }

    /**
     * Xóa time group
     */
    public void removeTimeGroup(TimeGroup timeGroup) {
        if (timeGroup != null) {
            this.timeGroups.remove(timeGroup);
            timeGroup.setDatasetGroupChild(null);
        }
    }

    // ==================== LIFECYCLE CALLBACKS ====================

    @PreUpdate
    protected void onUpdate() {
        this.updateAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updateAt == null) {
            this.updateAt = LocalDateTime.now();
        }
        if (this.version == null) {
            this.version = 0;
        }
        if (this.isHaveData == null) {
            this.isHaveData = false;
        }
    }

    // ==================== EQUALS & HASHCODE ====================
    // Chỉ dùng ID để tránh vòng lặp vô hạn

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatasetGroup)) return false;
        DatasetGroup that = (DatasetGroup) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}