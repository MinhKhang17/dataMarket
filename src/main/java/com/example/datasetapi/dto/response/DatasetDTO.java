package com.example.datasetapi.dto.response;

import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.model.Dataset.DatasetPricing;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DatasetDTO {
    private long datasetId;
    private int version;
    private ProviderDto provider;
    private List<DatasetPLanWithPricingDTO> datasetPLanWithPricingDTO;
    private LocalDate datasetTime;
    private String description;
    private String title;
}
