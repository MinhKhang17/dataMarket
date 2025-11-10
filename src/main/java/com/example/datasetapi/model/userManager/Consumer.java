package com.example.datasetapi.model.userManager;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "consumer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consumer {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "consumer_consumer_type",
            joinColumns = @JoinColumn(name = "consumer_id"),
            inverseJoinColumns = @JoinColumn(name = "consumer_type_id")
    )
    private List<ConsumerType> consumerTypes;

    @Column(name = "is_do_survey", nullable = false)
    private boolean doSurvey = false;
}


