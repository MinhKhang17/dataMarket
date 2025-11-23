package com.example.datasetapi.dto.request;

import lombok.Data;

@Data
public class WithdrawRequest {
    private Long amount;
    private Long BankAccountId;
    private String otp;
}
