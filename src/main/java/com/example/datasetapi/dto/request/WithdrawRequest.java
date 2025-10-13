package com.example.datasetapi.dto.request;

import lombok.Data;

@Data
public class WithdrawRequest {
    private Long walletId;
    private Long amount;
}
