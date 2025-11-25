package com.example.datasetapi.model.order;

import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.model.userManager.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItem> items;

    @Column(nullable = false)
    private double totalAmount;

    @Enumerated(EnumType.STRING)
    private PricingMethod purchaseMethod;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
