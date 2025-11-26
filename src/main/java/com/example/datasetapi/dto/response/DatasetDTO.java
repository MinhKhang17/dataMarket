package com.example.datasetapi.dto.response;

import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.model.dataset.Category;
import com.example.datasetapi.model.dataset.DatasetType;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DatasetDTO {
    private long datasetId;
    private int version;
    private ProviderDto provider;
    private List<DatasetPLanWithPricingDTO> datasetPLanWithPricingDTO;
    private LocalDate datasetTime;
    private String description;
    private String title;
    private String province;
    private String commune;
    private List<Category> category;
    private LocalDate date;
    private long row_amount;
    private DatasetSourceType datasetType;
    private BigInteger fileSize;
    private List<String> previewHeaders;
    private List<Map<String, String>> previewRows;
    private LocalDateTime purchasedAt;
}
