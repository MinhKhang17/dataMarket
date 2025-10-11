package com.example.datasetapi.dto.response;

import com.example.datasetapi.model.Dataset.Dataset;
//import com.example.datasetapi.model.userManager.Address;
import com.example.datasetapi.model.location.Location;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DatasetReposonseDto {

    private long datasetGroupId;

    private Location location;

    private int lasted_version;

    private LocalDateTime lasted_upload;

    private DatasetTypeDto datasetTypeDto;

    private List<DatasetDTO> datasetDTOS;


}
