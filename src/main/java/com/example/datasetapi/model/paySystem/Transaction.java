package com.example.datasetapi.model.paySystem;

import com.example.datasetapi.enums.TransferType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table
public class Transaction {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private TransferType type;

    @Column
    private double amount;

    @ManyToOne
    @JoinColumn
    private Wallet wallet;
}
