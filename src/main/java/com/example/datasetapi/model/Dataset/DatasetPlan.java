package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Table
@Entity
@Data
public class DatasetPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    private Dataset dataset;

    @OneToMany(cascade = CascadeType.ALL)
    private List<DatasetPricing> datasetPricingList;

    @Enumerated(EnumType.STRING)
    private DatasetPack datasetPack;


}
