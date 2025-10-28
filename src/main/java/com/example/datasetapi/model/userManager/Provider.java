package com.example.datasetapi.model.userManager;

import com.example.datasetapi.model.location.Commune;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "Provider")
public class Provider {

    @Id
    private long id; // id dùng chung với User

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "provider_registration_id") // FK tới ProviderRegistration
    private ProviderRegistration providerRegistration;

    @ManyToMany()
    @JsonIgnore
    @JoinTable(
            name = "provider_commune",
            joinColumns = @JoinColumn(name = "provider_id"),
            inverseJoinColumns = @JoinColumn(name = "commune_id")
    )
    private List<Commune> communes;
}
