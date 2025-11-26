package com.example.datasetapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueResponse {
    private Double total;
    private Double oneTime;
    private Double subscription;
    private Double api;
}
