package com.example.datasetapi.dto.response;

import com.example.datasetapi.model.Role;
import lombok.Data;

@Data
public class UserInformationResponseForAuthMe {
    private long user_id;
    private String user_name;
    private String email;
    private String role;
}
