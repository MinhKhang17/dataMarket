package com.example.datasetapi.dto.request;

import com.example.datasetapi.dto.service.ProviderIdentityDocumentDTO;
import lombok.Data;

import java.util.List;

@Data
public class LocationRegistrationRequest {
    private String idCommune;
    private List<ProviderIdentityDocumentDTO> identityDocuments;
}
