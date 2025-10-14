package com.example.datasetapi.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DatasetChildGroupDTO {
    private CommuneDTO communeDto;
    private List<DatasetDTO> datasetDtoList;
}
