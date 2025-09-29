package com.example.datasetapi.model.Dataset;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Table
@Entity
@Data
public class DatasetType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String name;

    @ManyToMany
    @JoinTable(
            name = "Type_Columns",
            joinColumns = @JoinColumn(name = "dataset_type_id"), // FK trỏ về DatasetType
            inverseJoinColumns = @JoinColumn(name = "dataset_type_column_id") // FK trỏ về DatasetTypeColumn
    )
    private List<DatasetTypeColumn> datasetTypeColumnList;}
