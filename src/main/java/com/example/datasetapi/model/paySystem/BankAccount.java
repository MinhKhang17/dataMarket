package com.example.datasetapi.model.paySystem;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table
public class BankAccount {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;


}
