package com.example.datasetapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ConsumerRequest {
    @NotBlank
    private List<Long> typeIds;
}
