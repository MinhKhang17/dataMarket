package com.example.datasetapi.model.dataset;

import com.example.datasetapi.model.userManager.Address;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.User;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.boot.autoconfigure.web.WebProperties;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Data
public class DatasetGroup {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(cascade = CascadeType.ALL)
    private Address address;

    @Column
    private int version = 0;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_type_id")
    private DatasetType datasetType;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Dataset> datasets = new ArrayList<Dataset>();
}
