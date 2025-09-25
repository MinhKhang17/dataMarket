package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.request.LoginRequest;
import com.example.datasetapi.dto.request.RegisterRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.User;
import com.example.datasetapi.model.paySystem.Wallet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

public interface UserService {
    ResponseEntity<ApiResponse> login(LoginRequest loginRequest, HttpServletResponse response);
    public ResponseEntity<ApiResponse> register(RegisterRequest registerRequest);

    User createUserForLoginByGoogleFlow(OAuth2User oAuth2User);

    ResponseEntity<ApiResponse> logout(HttpServletResponse response);

    Optional<User> findUserById(long userId);



    ResponseEntity<?> getWalletAmountFromToken(HttpServletRequest token);
}
