// ==================== DatasetPricing.java ====================
package com.example.datasetapi.model.dataset;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "dataset_pricing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatasetPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double price;

    @Column
    private Double pricePerRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_rule_id")
    @JsonIgnore
    private PricingRule pricingRule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DatasetPack datasetPack = DatasetPack.UNDETERMINED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PricingMethod pricingMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_plan_id")
    @JsonIgnore
    private DatasetPlan datasetPlan;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatasetPricing)) return false;
        DatasetPricing that = (DatasetPricing) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DatasetPricing{" +
                "id=" + id +
                ", price=" + price +
                ", datasetPack=" + datasetPack +
                ", pricingMethod=" + pricingMethod +
                '}';
    }
}