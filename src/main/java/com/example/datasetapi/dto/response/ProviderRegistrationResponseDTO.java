package com.example.datasetapi.dto.response;

import com.example.datasetapi.enums.DocumentType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProviderRegistrationResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String registrationStatus;
    private LocalDateTime createdAt;
}
