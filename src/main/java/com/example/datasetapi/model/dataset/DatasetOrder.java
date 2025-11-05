package com.example.datasetapi.model.dataset;

import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.model.userManager.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor

public class DatasetOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "order_datasets_items",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "dataset_id")
    )
    @Column
    private List<Dataset> datasets;

    @Column(nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    private PricingMethod purchaseMethod;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
