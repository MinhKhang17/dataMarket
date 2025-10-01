package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.userManager.Token;
import com.example.datasetapi.model.userManager.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;

public interface TokenService {
    public Token saveToken(String token, User user);
    public String generateAccessToken(User user);
    public String generateRefreshToken(String token);

    ResponseEntity<ApiResponse> refrestAccessToken(HttpServletRequest request, HttpServletResponse response);

    String resolveToken(HttpServletRequest request);

    @Transactional
    public void deleteByUserId(long userId);
}
