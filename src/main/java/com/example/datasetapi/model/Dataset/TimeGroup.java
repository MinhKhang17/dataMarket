package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.model.UserManager.Provider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table
public class TimeGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;
    private int month;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_group_child_id")
    private DatasetGroup datasetGroupChild;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @OneToMany(mappedBy = "timeGroup", orphanRemoval = true)
    @JsonIgnore
    private List<Dataset> datasets = new ArrayList<>();

    @Column
    private double price =0.0;
    @Column
    private long Row_Count;
    public static TimeGroup fromDate(LocalDate date) {
        TimeGroup tg = new TimeGroup();
        tg.setYear(date.getYear());
        tg.setMonth(date.getMonthValue());
        return tg;
    }
    public static LocalDate toDate(TimeGroup tg) {
        return LocalDate.of(tg.getYear(), tg.getMonth(), 1);
    }
}
