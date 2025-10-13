package com.example.datasetapi.model.location;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id", nullable = true)
    private Commune commune;

    public String getFullLocation() {
        if (commune != null && province != null) {
            return commune.getName() + ", " + province.getName();
        } else if (province != null) {
            return province.getName();
        } else if (commune != null) {
            return commune.getName();
        } else {
            return "";
        }
    }
}

