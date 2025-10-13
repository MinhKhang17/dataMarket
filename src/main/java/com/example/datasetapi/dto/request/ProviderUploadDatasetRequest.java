package com.example.datasetapi.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class ProviderUploadDatasetRequest {
    private long dataset_Information_Id;
    private String title;
    private String description;
    private long provider_location_id;
    private String dataset_time;
}
