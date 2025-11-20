package com.example.datasetapi.model.dataset;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import jakarta.persistence.*;
import lombok.Data;

@Table
@Entity
@Data
public class DatasetPricing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double price;

    private double pricePerRequest;
//    @Enumerated(EnumType.STRING)
//    private PricingType pricingType = PricingType.UNDETERMINED;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_rule_id")
    private PricingRule pricingRule;

    @Enumerated(EnumType.STRING)
    private DatasetPack datasetPack =DatasetPack.UNDETERMINED;

    @Enumerated
    private PricingMethod pricingMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_plan_id") // tên cột FK
    private DatasetPlan datasetPlan;
}
