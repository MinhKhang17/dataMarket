package com.example.datasetapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ConsumerRequest {
    private List<Long> typeIds;
    private String otherType;
}
