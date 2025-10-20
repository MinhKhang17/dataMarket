package com.example.datasetapi.dto.response;

import lombok.Data;

import java.time.LocalDate;
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
