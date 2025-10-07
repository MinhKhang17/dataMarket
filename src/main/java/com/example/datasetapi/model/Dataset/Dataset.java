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

@Column
private DatasetStatus datasetStatus;

@Column
private String fileKey;

@ManyToOne
@JoinColumn(name = "dataset_group_id")
private DatasetGroup datasetGroup;

}
