package com.example.datasetapi.model.UserManager;

import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
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

    private String fullName;
    private String email;
    private String phoneNumber;

    private String addressLine;
    private String city;
    private String district;
    private String ward;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus registrationStatus; // PENDING / APPROVED / REJECTED

    private Instant createdAt;
    private Instant updatedAt;

    // Liên kết sang giấy tờ định danh
    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProviderIdentityDocument> identityDocuments;
    @OneToOne
    private Provider provider;
}
