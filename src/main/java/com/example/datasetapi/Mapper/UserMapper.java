package com.example.datasetapi.Mapper;

import com.example.datasetapi.dto.response.UserInformationResponseForAuthMe;
import com.example.datasetapi.model.userManager.User;

public class UserMapper {
    public static UserInformationResponseForAuthMe toUserInformationResponseForAuthMeDTO(User user) {

        UserInformationResponseForAuthMe userInformationResponse = new UserInformationResponseForAuthMe();
        userInformationResponse.setUser_name(user.getUsername());
        userInformationResponse.setEmail(user.getEmail());
        userInformationResponse.setUser_id(user.getId());
        userInformationResponse.setRole(user.getRole().getName());
        return userInformationResponse;

    }
}
