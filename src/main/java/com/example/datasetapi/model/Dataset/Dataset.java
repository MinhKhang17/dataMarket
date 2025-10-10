package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.model.userManager.Provider;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
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

@Column
private LocalDateTime created_at=LocalDateTime.now();
@Column
private LocalDateTime updated_at=LocalDateTime.now();
@Column
private String title;

@ManyToOne(cascade = CascadeType.ALL)
@JoinColumn(name = "dataset_group_id")
private DatasetGroup datasetGroup;

@ManyToOne
    private Provider provider;
}
