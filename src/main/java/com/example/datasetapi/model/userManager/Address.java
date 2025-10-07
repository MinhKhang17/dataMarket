package com.example.datasetapi.model.userManager;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String ward;
    @Column
    private String district;
    @Column
    private String province;

    public String FulAddress(Address address) {

        return this.ward + " " + this.district + " " + this.province;
    }
}
