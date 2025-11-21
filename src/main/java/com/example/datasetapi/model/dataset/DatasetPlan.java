// ==================== DatasetPlan.java ====================
package com.example.datasetapi.model.dataset;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "dataset_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatasetPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    @JsonIgnore
    private Dataset dataset;

    @OneToMany(mappedBy = "datasetPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DatasetPricing> datasetPricingList = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DatasetPack datasetPack;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PricingMethod pricingMethod;

    // ✅ Helper method để thêm pricing
    public void addDatasetPricing(DatasetPricing pricing) {
        datasetPricingList.add(pricing);
        pricing.setDatasetPlan(this);
    }

    // ✅ Helper method để xóa pricing
    public void removeDatasetPricing(DatasetPricing pricing) {
        datasetPricingList.remove(pricing);
        pricing.setDatasetPlan(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatasetPlan)) return false;
        DatasetPlan that = (DatasetPlan) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DatasetPlan{" +
                "id=" + id +
                ", datasetPack=" + datasetPack +
                ", pricingMethod=" + pricingMethod +
                '}';
    }
}
