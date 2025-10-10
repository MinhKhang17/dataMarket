package com.example.datasetapi.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DatasetDTO {
    private long datasetId;
    private int version;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
