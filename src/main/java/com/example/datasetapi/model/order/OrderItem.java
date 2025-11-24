package com.example.datasetapi.model.order;

import com.example.datasetapi.enums.Datasets.SubType;
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
    private String itemName;

    @Column
    private double priceAtPurchase;

    @Column
    private SubType subType;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Orders order;
}
