package com.example.datasetapi.dto.response;

import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.userManager.Address;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DatasetReposonseDto {

    private long datasetGroupId;

    private Address address;

    private int lasted_version;

    private LocalDateTime lasted_upload;

    private DatasetTypeDto datasetTypeDto;

    private List<DatasetDTO> datasetDTOS;


}
