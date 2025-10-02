package com.example.datasetapi.model.userManager;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "consumer_type")
public class ConsumerType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "consumerTypes", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Consumer> consumers;
}
