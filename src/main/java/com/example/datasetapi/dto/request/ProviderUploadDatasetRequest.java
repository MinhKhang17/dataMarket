package com.example.datasetapi.dto.request;

import lombok.Data;

@Data
public class ProviderUploadDatasetRequest {
    private String title;
    private String description;
    private String commune_id;
    private String dataset_time;
}
