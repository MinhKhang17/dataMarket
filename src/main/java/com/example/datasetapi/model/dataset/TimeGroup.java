package com.example.datasetapi.model.dataset;

import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.model.userManager.Provider;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class TimeGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;
    private int month;
    private int day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_group_child_id")
    private DatasetGroup datasetGroupChild;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @OneToMany(mappedBy = "timeGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dataset> datasets = new ArrayList<>();

    @Column
    private double price =0.0;

    @Column
    private long Row_Count;

    @Enumerated(EnumType.STRING)
    private DatasetSourceType datasetSourceType;
    public static TimeGroup fromDate(LocalDate date) {
        TimeGroup tg = new TimeGroup();
        tg.setYear(date.getYear());
        tg.setMonth(date.getMonthValue());
        tg.setDay(date.getDayOfMonth());
        return tg;
    }
    public static LocalDate toDate(TimeGroup tg) {
        return LocalDate.of(tg.getYear(), tg.getMonth(), tg.getDay());
    }
}
