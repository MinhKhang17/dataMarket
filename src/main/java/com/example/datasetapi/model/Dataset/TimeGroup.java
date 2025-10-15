package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.model.UserManager.Provider;
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
