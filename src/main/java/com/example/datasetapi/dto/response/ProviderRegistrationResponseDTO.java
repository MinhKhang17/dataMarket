package com.example.datasetapi.dto.response;

import com.example.datasetapi.repository.ProviderIndentityDocumentRepository;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProviderRegistrationResponseDTO {
    private Long id;
    private String organizationName;
    private String taxId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String registrationStatus;
    private LocalDateTime createdAt;
    private List<ProviderIndentityDocumentDTO> providerIndentityDocumentDTOList;
    private String location;
}
