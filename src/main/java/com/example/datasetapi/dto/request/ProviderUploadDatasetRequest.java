package com.example.datasetapi.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProviderUploadDatasetRequest {
    private long dataset_Information_Id;
    private String title;
    private String description;
    private long provider_address_id;
}
