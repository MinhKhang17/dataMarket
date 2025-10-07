package com.example.datasetapi.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProviderUploadDatasetRequest {
    private MultipartFile file;
    private String title;
    private long dataset_type_id;
    private String description;
    private long provider_address_id;
}
