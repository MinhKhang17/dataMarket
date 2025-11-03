package com.example.datasetapi.model.dataset;

import com.example.datasetapi.enums.Datasets.DatasetGroupType;
import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
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

    @Enumerated(EnumType.STRING)
    private DatasetGroupType datasetGroupType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_type_id")
    private DatasetType datasetType;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Dataset> datasets = new ArrayList<Dataset>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatasetGroup> datasetGroups = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DatasetGroup parent;

    //sẽ null khi là dataset group con
    @ManyToOne
    private Province province;
    //sẽ null khi là dataset group cha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private Commune commune;

    @Column
    private LocalDateTime updateAt = LocalDateTime.now();
    private boolean isHaveData =false;
    @OneToMany(mappedBy = "datasetGroupChild", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeGroup> timeGroups = new ArrayList<>();
@Enumerated(EnumType.STRING)
   private DatasetSourceType datasetSourceType;
}
