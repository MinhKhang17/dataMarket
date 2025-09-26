package com.example.datasetapi.model.UserManager;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table
public class Provider {
@Id
@GeneratedValue(strategy= GenerationType.IDENTITY)
private String id;
@Column
private String bankAccount;
@OneToOne(cascade=CascadeType.ALL)
private User user;
}
