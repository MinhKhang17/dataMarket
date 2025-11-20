package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.dataset.DownloadToken;
import com.example.datasetapi.model.userManager.Token;
import com.example.datasetapi.model.userManager.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface TokenService {
     Token saveToken(String token, User user);
     String generateAccessToken(User user);
     String generateRefreshToken(String token);

    ResponseEntity<ApiResponse> refrestAccessToken(HttpServletRequest request, HttpServletResponse response);

    String resolveToken(HttpServletRequest request);

    @Transactional
    void deleteByUserId(long userId);

    long getUserIdFromRequest(HttpServletRequest request);

    DownloadToken findDownloadTokenById(UUID tokenId);

    String generateVerifyEmailToken(User user);

    String verifyEmailToken(String token);

//    public ResponseEntity<ApiResponse> getDownloadToken(long datasetId, HttpServletRequest request);

}
