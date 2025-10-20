package com.example.datasetapi.model.dataset;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.model.userManager.Provider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "dataset")
public class Dataset {
@Id
@GeneratedValue(strategy= GenerationType.IDENTITY)
private Long id;

@Column
private String name;

@Column
private String description;

@Column
@Enumerated(EnumType.STRING)
private DatasetStatus datasetStatus = DatasetStatus.PENDING;


@Column
private String fileKey;

@Column
private int version;

    // ✅ ĐỔI TÊN FIELD NÀY
    @Column(name = "created_at")  // Map tới column created_at trong DB
    private LocalDateTime createdAt = LocalDateTime.now();  // Field name trong Java

    // ✅ ĐỔI TÊN FIELD NÀY
    @Column(name = "updated_at")  // Map tới column updated_at trong DB
    private LocalDateTime updatedAt;  // Field name trong Java
@Column
private String title;

@ManyToOne(cascade = CascadeType.ALL)
@JsonIgnore
@JoinColumn(name = "dataset_group_id")
private DatasetGroup datasetChildGroup;

@ManyToOne(cascade = CascadeType.ALL)
    private Provider provider;
@Column
    @Enumerated(EnumType.STRING)
    private DatasetPack datasetPack=DatasetPack.UNDETERMINED;

@ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "time_group_id")
    private TimeGroup timeGroup;


    @OneToMany(mappedBy = "dataset", fetch = FetchType.LAZY)
    @JsonManagedReference  // Phía parent - sẽ serialize
    private List<DatasetPlan> datasetPlans = new ArrayList<>();

    @OneToOne
    private DatasetInformation datasetInformation;
}
