package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.dataset.DownloadToken;
import com.example.datasetapi.model.userManager.Token;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.DownloadTokenRepository;
import com.example.datasetapi.repository.TokenRepository;
import com.example.datasetapi.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {
    @Override
    public long getUserIdFromRequest(HttpServletRequest request) {
        return jwtUtil.getUserIdFromToken(resolveToken(request));
    }

    @Autowired
    private  DownloadTokenRepository downloadTokenRepository;

    @Override
    public DownloadToken findDownloadTokenById(UUID tokenId) {
        return downloadTokenRepository.findById(tokenId)
                .orElseThrow(() -> new EntityNotFoundException("DownloadToken not found with id: " + tokenId));
    }

@Autowired
    private TokenRepository tokenRepository;
@Autowired
    private JwtUtil jwtUtil;

    @Override
    public ResponseEntity<ApiResponse> refrestAccessToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String refreshTokenRequest = "";
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh_token")) {
                    refreshTokenRequest = cookie.getValue();
                    break;
                }
            }

            Token token = tokenRepository.findByToken(refreshTokenRequest);

            if (token != null) {

                String accessToken = generateAccessToken(token.getUser());

                return ResponseEntity.ok().body(new ApiResponse(true, "Token refreshed successfully", accessToken));
            } else {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.TOKEN_NOT_FOUND);
            }

        }
        throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.REFRESH_TOKEN_INVALID);
    }





    @Override
    public Token saveToken(String token, User user) {
        return tokenRepository.save(new Token(token, user));
    }

    @Override
    public String generateAccessToken(User user) {
        return jwtUtil.generateAccessToken(user);
    }

    @Override
    public String generateRefreshToken(String token) {
        return jwtUtil.generateRefreshToken(token);
    }

    @Transactional
    public void saveTokenByUserId(String refreshToken, Long userId) {
        Token token = new Token();
        token.setToken(refreshToken);

        // Create user reference without loading full entity
        User userReference = new User();
        userReference.setId(userId);
        token.setUser(userReference);


        tokenRepository.save(token);
    }

    public String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // bỏ "Bearer "
        }
        return null;
    }

    @Override
    public void deleteByUserId(long userId) {
        tokenRepository.deleteByUser_Id(userId);
    }


//    @Override
//    public ResponseEntity<ApiResponse> getDownloadToken(long datasetId, HttpServletRequest request) {
//        try {
//            long userId = jwtUtil.getUserIdFromToken(resolveToken(request));
//
//            if (userId == -1) {
//                return ResponseEntity
//                        .status(HttpStatus.UNAUTHORIZED)
//                        .body(new ApiResponse(false, "Invalid or expired token", null));
//            }
//
//            DownloadToken downloadToken = jwtUtil.generateDowloadToken(userId, datasetId, 30);
//            return ResponseEntity.ok()
//                    .body(new ApiResponse(true, "Download token generated successfully", downloadToken));
//
//        } catch (Exception e) {
//            // Log the error (add appropriate logger)
//            // logger.error("Error generating download token for fileKey: " + fileKey, e);
//
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse(false, "Failed to generate download token: " + e.getMessage(), null));
//        }
//    }
}
