package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.enums.Datasets.DatasetStatus;
import jakarta.persistence.*;
import lombok.Data;

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

@ManyToMany()
@JoinTable(
        name = "dataset_category",
        joinColumns = @JoinColumn(name = "dataset_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
)
private List<Category> categories;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "dataset_type_id")
private DatasetType datasetType;

@Column
private DatasetStatus datasetStatus;

@Column
private String fileKey;
}
