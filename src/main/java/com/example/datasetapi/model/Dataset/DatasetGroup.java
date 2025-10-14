package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.model.UserManager.Provider;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Data
public class DatasetGroup {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;



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
    @OneToMany(cascade = CascadeType.ALL)
    private List<DatasetGroup> datasetGroups = new ArrayList<DatasetGroup>();

    //sẽ null khi là dataset group con
    @ManyToOne
    private Province province;
    //sẽ null khi là dataset group cha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comune_id")
    private Commune commune;
}
