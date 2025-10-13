package com.example.datasetapi.model.dataset;

import jakarta.persistence.*;
import lombok.Data;

@Table
@Entity
@Data
public class DatasetTypeColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String columnName;

}
