package com.example.datasetapi.model.UserManager;

import com.example.datasetapi.enums.Datasets.SubType;
import com.example.datasetapi.model.Dataset.PricingRule;
import jakarta.persistence.*;
import lombok.Data;
import org.checkerframework.checker.units.qual.C;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
