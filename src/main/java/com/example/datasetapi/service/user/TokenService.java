package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.response.UserLoginData;
import com.example.datasetapi.model.Token;
import com.example.datasetapi.model.User;

public interface TokenService {
    public Token saveToken(String token, User user);
    public String generateAccessToken(UserLoginData user);
    public String generateRefreshToken(String token);
}
