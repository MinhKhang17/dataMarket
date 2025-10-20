package com.example.datasetapi.model.userManager;

import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.model.location.Commune;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
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

    @ManyToOne
    private Commune commune;

    private String addressLine;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus registrationStatus; // PENDING / APPROVED / REJECTED

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt =  LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProviderIdentityDocument> identityDocuments;

    @OneToOne
    private Provider provider;
}
