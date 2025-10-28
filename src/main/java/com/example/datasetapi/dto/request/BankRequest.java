package com.example.datasetapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankRequest {
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
}
