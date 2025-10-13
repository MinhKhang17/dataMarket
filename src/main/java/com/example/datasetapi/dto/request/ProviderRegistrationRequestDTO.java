package com.example.datasetapi.dto.request;

import com.example.datasetapi.dto.service.ProvierIdentityDocumentDTO;
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

    // Thông tin dữ liệu cung cấp
    private String dataType;
    private String dataSource;
    private String dataProcessingStatus;

    // Danh sách giấy tờ định danh
    private List<ProvierIdentityDocumentDTO> identityDocuments;

}
