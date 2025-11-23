package com.example.datasetapi.dto.response;

import lombok.Data;

@Data
public class UserInformationResponseForAuthMe {
    private long user_id;
    private String user_name;
    private String email;
    private String role;
    private boolean isHaveSub = false;
    private String subType;
    private ConsumerSubResponseDTO consumerSubInfo;
}
