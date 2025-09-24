package com.example.datasetapi.dto.request;

import lombok.Data;

@Data
public class CreateVnpayPaymentRequest {
    private String amount;
    private String orderType;
    private String orderInfo;
    private String bankCode;
    private String language;
    private Long userId;

    // getters/setters

}
