package com.example.datasetapi.model.paySystem;

import com.example.datasetapi.model.userManager.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class Wallet {
@Id
@GeneratedValue(strategy= GenerationType.IDENTITY)
private int id;
@Column
private double amount;
@Column
private double holdBalance;
@OneToOne
    private User user;

}
