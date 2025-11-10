package com.example.datasetapi.model.userManager;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "consumer_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "consumerTypes", fetch = FetchType.LAZY)
    private List<Consumer> consumers;
}

