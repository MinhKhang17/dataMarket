package com.example.datasetapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawListResponse {
    private Long id;
    private String status;
    private Long amount;
    private Instant createdAt;
    private Instant updatedAt;
}
