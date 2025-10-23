package com.example.datasetapi.dto.response;

import com.example.datasetapi.model.Dataset.TimeGroup;
import lombok.Data;

import java.util.List;

@Data
public class DatasetChildGroupDTO {
    private long datasetChildGroupId;
    private CommuneDTO communeDto;
    private List<DatasetDTO> datasetDtoList;
    private List<TimeGroupDTO> timeGroupDtoList;
}
