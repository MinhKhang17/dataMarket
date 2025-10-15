package com.example.datasetapi.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProviderUploadDatasetRequest {
    private String title;
    private String description;
    private String commune_id;
    private String dataset_time;
    private String datasetTime;
}
