package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.request.LoginRequest;
import com.example.datasetapi.dto.request.ProviderRegistrationRequestDTO;
import com.example.datasetapi.dto.request.RegisterRequest;
import com.example.datasetapi.dto.request.UpdatePasswordRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.UserManager.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Service
public interface UserService {
    ResponseEntity<ApiResponse> login(LoginRequest loginRequest, HttpServletResponse response);

    public ResponseEntity<ApiResponse> register(RegisterRequest registerRequest);
    public  ResponseEntity<ApiResponse> updatePassword(UpdatePasswordRequest updatePasswordRequest);

    User createUserForLoginByGoogleFlow(OAuth2User oAuth2User);

    ResponseEntity<ApiResponse> logout(HttpServletResponse response);

    Optional<User> findUserById(long userId);


    ResponseEntity<ApiResponse> ProviderRegistrationProcess(ProviderRegistrationRequestDTO providerRegistrationDTO);

    ResponseEntity<ApiResponse> getUserInformationFromRequest(HttpServletRequest request);
    ResponseEntity<?> getWalletAmountFromToken(HttpServletRequest token);
}
