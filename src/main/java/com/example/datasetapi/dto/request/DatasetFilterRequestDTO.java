package com.example.datasetapi.dto.request;


import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetFilterRequestDTO {
    // Location filters
    private String provinceId;
    private Long communeId;

    // Time filters
    private Integer year;
    private Integer month;
    private Integer day;

    // Date range filters
    private Integer startYear;
    private Integer startMonth;
    private Integer startDay;

    private Integer endYear;
    private Integer endMonth;
    private Integer endDay;

    // Additional filters
    private Long providerId;
    private Long datasetTypeId;
    private DatasetStatus datasetStatus;
    private DatasetPack datasetPack;

    // Pagination
    private Integer page = 0;
    private Integer size = 20;

    // Sorting
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
