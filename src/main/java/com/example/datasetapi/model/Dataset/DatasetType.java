package com.example.datasetapi.model.Dataset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Table
@Entity
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    private List<DatasetTypeColumn> datasetTypeColumnList;

    @ManyToMany()
    @JoinTable(
            name = "dataset_type_category",
            joinColumns = @JoinColumn(name = "dataset_type_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    @Column
    private String keyFile;
}
