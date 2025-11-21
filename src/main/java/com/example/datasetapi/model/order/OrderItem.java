package com.example.datasetapi.model.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long datasetId;

    private String datasetName;
    private double priceAtPurchase;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
