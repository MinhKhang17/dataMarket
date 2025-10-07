package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.enums.Datasets.DatasetStatus;
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
private DatasetStatus datasetStatus;

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
@Column()
@Enumerated(EnumType.STRING)
private DatasetStatus status ;

@ManyToOne(cascade = CascadeType.ALL)
@JoinColumn(name = "dataset_group_id")
private DatasetGroup datasetGroup;

}
