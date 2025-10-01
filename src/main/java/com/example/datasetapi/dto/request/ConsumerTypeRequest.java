package com.example.datasetapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConsumerTypeRequest {
    @NotBlank
    private String name;
}
