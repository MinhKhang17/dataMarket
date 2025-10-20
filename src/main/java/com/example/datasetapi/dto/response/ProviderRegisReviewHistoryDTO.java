package com.example.datasetapi.dto.response;

import lombok.Data;

@Data
public class ProviderRegisReviewHistoryDTO {

    String providerRegisReviewHistoryId;
    UserDto provider;
    boolean isAccept;
    String reason;
    private UserDto admin;
}
