package com.example.datasetapi.dto.service;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProviderIdentityDocumentDTO {
    private String type;             // CCCD_FRONT, CCCD_BACK, BUSINESS_LICENSE
    private MultipartFile file;      // Ảnh upload trực tiếp

}
