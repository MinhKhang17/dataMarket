package com.example.datasetapi.dto;

import com.example.datasetapi.dto.response.DatasetDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderRevenueDTO {
    private long providerRevenueId;
    private DatasetDTO dataset;
    private double revenue;
    private LocalDateTime createdAt;
    public ProviderRevenueDTO(DatasetDTO datasetDTO, double revenueAmount, Long id,LocalDateTime createdAt) {
        this.dataset = datasetDTO;
        this.revenue = revenueAmount;
        this.providerRevenueId = id;
        this.createdAt = createdAt;
    }
}