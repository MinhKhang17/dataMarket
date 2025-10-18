package com.example.datasetapi.model.paySystem;

import com.example.datasetapi.model.UserManager.User;
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
@OneToOne
    private User user;

}
