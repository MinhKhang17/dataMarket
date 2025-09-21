package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.UserLoginData;
import com.example.datasetapi.model.Token;
import com.example.datasetapi.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface TokenService {
    public Token saveToken(String token, User user);
    public String generateAccessToken(User user);
    public String generateRefreshToken(String token);

    ResponseEntity<ApiResponse> refrestAccessToken(HttpServletRequest request, HttpServletResponse response);

}
