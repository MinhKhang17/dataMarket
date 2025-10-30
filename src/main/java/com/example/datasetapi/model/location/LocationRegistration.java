package com.example.datasetapi.model.location;


import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.ProviderIdentityDocument;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "location_registration")
public class LocationRegistration {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Provider provider;

    @ManyToOne
    private Commune commune;

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ProviderIdentityDocument> providerIdentityDocument;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

}
