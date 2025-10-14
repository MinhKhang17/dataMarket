package com.example.datasetapi.model.UserManager;

import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Entity
@Data
@Table(name = "provider_registration")
public class ProviderRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== Thông tin cơ bản =====
    private String fullName;
    private String email;
    private String phoneNumber;

    // ===== Thông tin tổ chức =====
    private String organizationName;
    private String taxId;

    @ManyToOne
    @JoinColumn(name = "id_province")
    private Province province;

    @ManyToOne
    @JoinColumn(name = "id_commune")
    private Commune commune;


    private String addressLine;
    private String city;
    private String district;
    private String ward;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus registrationStatus; // PENDING / APPROVED / REJECTED

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProviderIdentityDocument> identityDocuments;
}
