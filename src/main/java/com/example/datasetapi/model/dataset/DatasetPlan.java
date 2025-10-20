package com.example.datasetapi.model.dataset;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Table
@Entity
@Data
public class DatasetPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    @JsonIgnore  // Bỏ qua khi serialize
    private Dataset dataset;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DatasetPricing> datasetPricingList;

    @Enumerated(EnumType.STRING)
    private DatasetPack datasetPack;

    @Enumerated(EnumType.STRING)
    private PricingMethod pricingMethod;


}
