package com.example.datasetapi.dto.request;

import lombok.Data;

@Data
public class ProviderUploadDatasetRequest {
    private long dataset_Information_Id;
    private String title;
    private String description;
    private long provider_location_id;
    private String dataset_time;
}
