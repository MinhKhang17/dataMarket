package com.example.datasetapi.model.location;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "provinces")
@Data
public class Province {

    @Id
    private String idProvince;

    private String name;
}
