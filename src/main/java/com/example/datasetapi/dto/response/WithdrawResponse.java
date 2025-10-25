package com.example.datasetapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawResponse {
    private Long id;
    private String status;
    private Long amount;
    private Instant createdAt;
    private Instant updatedAt;
    private int walletId;
    private String reason;
    private String proofImageUrl;
    private String bank;
    private String accountNumber;

}
