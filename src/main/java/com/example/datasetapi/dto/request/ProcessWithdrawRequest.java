package com.example.datasetapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessWithdrawRequest {
    private Long id;
    private String reason;
    private String proofImageUrl;
}
