package com.example.springdatajpa_slot1.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String roleName;

    @OneToMany(mappedBy = "role")
    private List<User> users;
}
