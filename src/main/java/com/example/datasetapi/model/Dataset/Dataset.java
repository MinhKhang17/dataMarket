package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.enums.Datasets.DatasetStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
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
        joinColumns = @JoinColumn(name = "dataset_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
)
private List<Category> categories;

    @ManyToMany
    @JoinTable(
            name = "dataset_dataset_type",
            joinColumns = @JoinColumn(name = "dataset_id"),
            inverseJoinColumns = @JoinColumn(name = "dataset_type_id")
    )
    private List<DatasetType> datasetTypeList = new ArrayList<>();

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private DatasetStatus status = DatasetStatus.PENDING;

@Column
private Long rowCount;

@Column
private String file_url;
}
