package com.example.datasetapi.dto.request;

import com.example.datasetapi.enums.Datasets.DatasetPack;
import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasetUpdateRequest {

    private String datasetName;
    private String title;
    private String description;

    private String fileKey;
    private DatasetStatus status;
    private DatasetPack datasetPack;
    private DatasetSourceType datasetSourceType;

    private Long datasetChildGroupId;
    private String communeId;

    private Long timeGroupId;

    private List<DatasetPricingUpdateRequest> pricingList;
}

