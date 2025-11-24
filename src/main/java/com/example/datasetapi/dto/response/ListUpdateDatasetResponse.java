package com.example.datasetapi.dto.response;

import com.example.datasetapi.enums.Datasets.DatasetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListUpdateDatasetResponse {
    private Long id;
    private String name;
    private DatasetStatus status;
}
