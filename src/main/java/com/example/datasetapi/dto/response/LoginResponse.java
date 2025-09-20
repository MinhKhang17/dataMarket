package com.example.datasetapi.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String username;
    private String password;
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {}
}
