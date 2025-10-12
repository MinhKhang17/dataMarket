package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Pricing_Rule") // 👈 Tên bảng chính xác
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PricingMethod method; // ONE_TIME | SUBSCRIPTION | API

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Column(name = "min_row")
    private Long minRow;

    @Column(name = "max_row")
    private Long maxRow;

    @Column(name = "base_price_per_row_vnd")
    private Double basePricePerRowVnd;

    @Column(name = "base_price_point")
    private Double basePricePoint;

    @Column(name = "row_limit")
    private Long rowLimit;

    @Column(name = "time_limit_day")
    private Integer timeLimitDay;

    @Column(name = "extra_cost_per_1k_point")
    private Double extraCostPer1kPoint;

    @Column(name = "allow_overage")
    private Boolean allowOverage;

    @Column(name = "request_limit")
    private Long requestLimit;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "provider_share")
    private Integer providerShare;

    @Column(name = "platform_share")
    private Integer platformShare;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column()
    @Enumerated(EnumType.STRING)
    private DatasetPack datasetPack;
}
