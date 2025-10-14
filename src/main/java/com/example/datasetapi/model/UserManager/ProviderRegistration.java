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

    @Column(name = "id_province", nullable = false)
    private String provinceId;

    @Column(name = "id_commune", nullable = false)
    private String communeId;

    private String addressLine;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus registrationStatus; // PENDING / APPROVED / REJECTED

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProviderIdentityDocument> identityDocuments;
}
