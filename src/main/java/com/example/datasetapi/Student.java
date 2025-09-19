package com.example.datasetapi;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "student")
@Data
public class Student {
    @Id
    @GeneratedValue()
    public int id;
    @Column(name = "name",unique=true)
    public String firstName;
    @Column(name = "last_name",unique=true)
    public String lastName;
}
