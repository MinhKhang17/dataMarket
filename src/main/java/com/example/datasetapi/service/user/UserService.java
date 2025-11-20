package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.ProviderRevenueDTO;
import com.example.datasetapi.dto.request.*;
import com.example.datasetapi.dto.response.ApiResponse;
//import com.example.datasetapi.model.userManager.Address;
import com.example.datasetapi.dto.response.UserDto;
import com.example.datasetapi.model.dataset.ProviderRevenue;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.Role;
import com.example.datasetapi.model.userManager.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    ResponseEntity<ApiResponse> login(LoginRequest loginRequest, HttpServletResponse response);
    ResponseEntity<ApiResponse> register(RegisterRequest registerRequest);
    ResponseEntity<ApiResponse> updatePassword(UpdatePasswordRequest updatePasswordRequest);

    User createUserForLoginByGoogleFlow(OAuth2User oAuth2User);

    ResponseEntity<ApiResponse> logout(HttpServletResponse response, HttpServletRequest request);

    User findUserById(long userId);

    @Transactional
    ResponseEntity<ApiResponse> providerRegistrationProcess(ProviderRegistrationRequestDTO providerRegistrationDTO);

    ResponseEntity<ApiResponse> getUserInformationFromRequest(HttpServletRequest request);

    ResponseEntity<?> getWalletAmountFromToken(HttpServletRequest token);

    Provider findProviderById(long providerId);

    boolean isExitsProvider(long providerId);

    ResponseEntity<?> getProviderCommune(HttpServletRequest request);

    Role findRoleByName(String provider);

    Provider saveProvider(Provider provider);

    void saveUser(User consumer);

    ResponseEntity<ApiResponse> locationRegistrationProcess(LocationRegistrationRequest locationRegistrationRequest);

    List<ProviderRevenueDTO> getProviderRevenue(HttpServletRequest request);

    void save(User user);

    List<UserDto> findAllUser();

    User banUser(Long id);

    User unBanUser(Long id);
}
