package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.enums.Datasets.PricingType;
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

    @ManyToOne(cascade = CascadeType.ALL)
    private DatasetPlan datasetPlan;
    @ManyToOne (cascade = CascadeType.ALL)
    private PricingRule pricingRule;
}
