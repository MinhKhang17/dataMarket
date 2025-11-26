package com.example.datasetapi.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatasetGroupResponse {
    private long parentGroupId;
    private long childGroupId;
    private String datasetType;
    private String province;
    private String commune;
}
