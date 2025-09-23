package com.example.springdatajpa_slot1.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Set;
import com.example.springdatajpa_slot1.model.Categories;
@Data
@Entity
@Table
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long book_id;

    @Column
    private String title;

    @Column
    private String ISBN;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToMany(mappedBy = "books")
    private List<Categories> Categories;

}
