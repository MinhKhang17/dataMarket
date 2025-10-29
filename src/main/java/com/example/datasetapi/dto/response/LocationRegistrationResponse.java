package com.example.datasetapi.dto.response;

import com.example.datasetapi.dto.service.ProviderIdentityDocumentDTO;
import lombok.Data;

import java.util.List;
@Data
public class LocationRegistrationResponse {
    private Long id;
    private String name;
    private String email;
    private String communeName;
    private String provinceName;
    private List<ProviderIdentityDocumentDTO> identityDocuments;
}
