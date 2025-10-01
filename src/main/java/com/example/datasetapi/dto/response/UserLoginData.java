package com.example.datasetapi.dto.response;

import com.example.datasetapi.model.userManager.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginData {
    private long id;
    private String email;
    private String password;
    private String username;
    List<Role>  roles = new ArrayList<>();
    public UserLoginData(Long id, String email, String password, String username) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
    }
}
