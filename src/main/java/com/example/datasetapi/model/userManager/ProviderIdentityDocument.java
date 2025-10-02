package com.example.datasetapi.model.userManager;

import com.example.datasetapi.enums.DocumentType;
import com.example.datasetapi.enums.VerificationStatus.VerificationStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "provider_identity_document")
public class ProviderIdentityDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private ProviderRegistration provider;

//    private String identityNumber;  // số CCCD/CMND

    // Chỉ lưu link ảnh
   private String image_url;

    private Instant UploadedAt;

    @Enumerated(EnumType.STRING)
    private VerificationStatus idCardVerificationStatus; // PENDING / VERIFIED / REJECTED

    private long manager_id;   // ai verify
    private Instant idCardRetentionExpiry; // ngày hết hạn lưu trữ

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
}
