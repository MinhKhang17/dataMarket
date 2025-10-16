package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.request.LoginRequest;
import com.example.datasetapi.dto.request.ProviderRegistrationRequestDTO;
import com.example.datasetapi.dto.request.RegisterRequest;
import com.example.datasetapi.dto.request.UpdatePasswordRequest;
import com.example.datasetapi.dto.response.ApiResponse;
//import com.example.datasetapi.model.userManager.Address;
import com.example.datasetapi.model.UserManager.Provider;
import com.example.datasetapi.model.UserManager.Role;
import com.example.datasetapi.model.UserManager.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    ResponseEntity<ApiResponse> login(LoginRequest loginRequest, HttpServletResponse response);
    ResponseEntity<ApiResponse> register(RegisterRequest registerRequest);
    ResponseEntity<ApiResponse> updatePassword(UpdatePasswordRequest updatePasswordRequest);

    User createUserForLoginByGoogleFlow(OAuth2User oAuth2User);

    ResponseEntity<ApiResponse> logout(HttpServletResponse response, HttpServletRequest request);

    User findUserById(long userId);


    @Transactional
    ResponseEntity<ApiResponse> ProviderRegistrationProcess(ProviderRegistrationRequestDTO providerRegistrationDTO);

    ResponseEntity<ApiResponse> getUserInformationFromRequest(HttpServletRequest request);
    ResponseEntity<?> getWalletAmountFromToken(HttpServletRequest token);

    Provider findProviderById(long providerId);

    boolean isExitsProvider(long providerId);

    ResponseEntity<?> getProviderCommune(HttpServletRequest request);

    Role findRoleByName(String provider);

    Provider saveProvider(Provider provider);
}
