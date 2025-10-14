package com.example.datasetapi.model.UserManager;

import com.example.datasetapi.model.location.Commune;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "Provider")
public class Provider {

    @Id
    private long id; // id dùng chung với User

    @OneToOne
    @MapsId // dùng cùng primary key với User
    @JoinColumn(name = "id") // tên cột khóa ngoại trùng với PK
    private User user;

    @Column
    private String bankAccount;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "provider_registration_id") // FK tới ProviderRegistration
    private ProviderRegistration providerRegistration;

    @ManyToMany()
    @JoinTable(
            name = "provider_commune",
            joinColumns = @JoinColumn(name = "provider_id"),
            inverseJoinColumns = @JoinColumn(name = "commune_id")
    )
    private List<Commune> communes;
}
