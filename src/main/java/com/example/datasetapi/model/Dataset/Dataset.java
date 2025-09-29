package com.example.datasetapi.model.Dataset;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table
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
        joinColumns = @JoinColumn(name = "category_id"),
        inverseJoinColumns = @JoinColumn(name = "dataset_id")
)
private List<Category> categories;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "dataset_type_id")
private DatasetType datasetType;

@Column
private String file_url;
}
