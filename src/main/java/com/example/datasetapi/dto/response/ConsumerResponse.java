package com.example.datasetapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerResponse {
    private Long id;
    private List<ConsumerTypeResponse> types;
    private boolean isDoSurvey;
}
