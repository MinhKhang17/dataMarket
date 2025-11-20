package com.example.datasetapi.dto.response;

import lombok.Data;

@Data
public class UserDto {
    private long id;
    private String username;
    private boolean isActive;
    private String email;
}
