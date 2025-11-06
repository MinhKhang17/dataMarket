package com.example.datasetapi.dto;

import com.example.datasetapi.dto.response.DatasetDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderRevenueDTO {
    private long providerRevenueId;
    private DatasetDTO dataset;
    private double revenue;

    public ProviderRevenueDTO(DatasetDTO datasetDTO, double revenueAmount, Long id) {
        this.dataset = datasetDTO;
        this.revenue = revenueAmount;
        this.providerRevenueId = id;
    }
}