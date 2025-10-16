package com.example.datasetapi.model.location;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "communes")
@Data
public class Commune {

    @Id
    private String idCommune;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_province", nullable = false)
    private Province province;

    public static String getFullLocation(Commune commune){
        return commune.getName()+" ,"+commune.getProvince().getName();
    }
}
