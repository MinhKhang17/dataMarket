package com.example.datasetapi.dto.request;

import com.example.datasetapi.dto.service.ProviderIdentityDocumentDTO;
import com.example.datasetapi.model.location.Province;
import lombok.Data;

import java.util.List;

@Data
public class ProviderRegistrationRequestDTO {
    // Thông tin cơ bản
    private String fullName;
    private String email;
    private String phoneNumber;

    // Thông tin tổ chức
    private String organizationName;
    private String taxId;

    private String provinceId;
    private String communeId;

    // Danh sách giấy tờ định danh
    private List<ProviderIdentityDocumentDTO> identityDocuments;

}
