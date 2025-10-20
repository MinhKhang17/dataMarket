package com.example.datasetapi.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DatasetTypeDto {
    private long id;
    private String name;
    List<CategoryDto> categoryDtoList;
}
