package com.example.datasetapi.model.dataset;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasetOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long datasetId;

    private String datasetName;
    private Long priceAtPurchase;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private DatasetOrder order;
}
