package com.example.datasetapi.model.userManager;

import com.example.datasetapi.enums.Datasets.SubType;
import com.example.datasetapi.model.dataset.PricingRule;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table
public class ConsumerSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    private User consumer;
    @Column
    private SubType subType;
    @Column
    long row_amount;
    @Column
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column
    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "pricing_rule_id")
    private PricingRule pricingRule;

    @Column
    private boolean isActive = false;
    @Column
    private boolean isUsing = false;
}
