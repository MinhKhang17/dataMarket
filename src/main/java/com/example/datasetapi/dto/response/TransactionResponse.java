package com.example.datasetapi.dto.response;

import com.example.datasetapi.enums.Datasets.BuyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private Long id;
    private String txnRef;
    private String transferType;
    private double amount;
    private BuyType buyType;
    private String createdAt;
}
