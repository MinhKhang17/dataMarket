package com.example.datasetapi.model.paySystem;

import com.example.datasetapi.enums.TransferType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Entity
@Table
public class Transaction {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    @Enumerated(EnumType.STRING)
    private TransferType type;

    @Column
    private double amount;

    @ManyToOne
    @JoinColumn
    private Wallet wallet;

    @Column(unique = true) // đảm bảo không trùng TxnRef
    private String txnRef;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
