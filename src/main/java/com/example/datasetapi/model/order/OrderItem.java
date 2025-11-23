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

    @Column
    private Long datasetId;

    @Column
    private String datasetName;

    @Column
    private double priceAtPurchase;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
