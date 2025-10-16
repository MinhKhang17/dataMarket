package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.model.UserManager.Provider;
import com.example.datasetapi.model.UserManager.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table
@Data
public class ReviewHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long reviewId;

    @OneToOne
    private Provider provider;

    @OneToOne(cascade = CascadeType.ALL)
    private Dataset dataset;

    @OneToOne
    private User moderator;
    @ManyToOne
    private User admin;

    @Column
    private LocalDateTime createAt =  LocalDateTime.now();

    @Column
    private String reason;

}
