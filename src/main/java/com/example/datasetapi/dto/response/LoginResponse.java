package com.example.datasetapi.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String accessToken;
    private String userName;
}
