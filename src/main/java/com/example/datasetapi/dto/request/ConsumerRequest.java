package com.example.datasetapi.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ConsumerRequest {
    private List<Long> typeIds;
    private String otherType;
}
