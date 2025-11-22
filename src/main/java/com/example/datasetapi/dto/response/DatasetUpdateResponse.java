package com.example.datasetapi.dto.response;

import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.model.dataset.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasetUpdateResponse {
    private Long id;
    private String name;
    private String title;
    private String description;
    private String commune;
    private String province;
    private Double price;
    private Double pricePerRequest;
    private String fileKey;
}
