package com.example.datasetapi.dto.response;

import lombok.Data;

@Data
public class RegisterResponse {

    String access_token;
    String refresh_token;
    String token_type;
    String scope;

}
