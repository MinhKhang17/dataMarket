package com.example.datasetapi.dto.service;

import com.example.datasetapi.enums.Datasets.SubType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BuySubInfoDTO {
    private LocalDateTime expiredDay;
    private long rowLimit;
    private SubType subType;
}
