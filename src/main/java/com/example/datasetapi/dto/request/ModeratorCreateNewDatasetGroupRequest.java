package com.example.datasetapi.dto.request;

import lombok.Data;

@Data
public class ModeratorCreateNewDatasetGroupRequest {
    public String commune_id;
    public long dataset_type_id;
}
