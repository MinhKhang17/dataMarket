package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.UserManager.Token;
import com.example.datasetapi.model.UserManager.User;
import com.example.datasetapi.repository.TokenRepository;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService{


    private TokenRepository tokenRepository;

    private JwtUtil jwtUtil;

    @Override
    public ResponseEntity<ApiResponse> refrestAccessToken(HttpServletRequest request, HttpServletResponse response) {
    Cookie[] cookies = request.getCookies();
    String refreshTokenRequest="";
    if(cookies!=null){
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("refresh_token")){
                refreshTokenRequest = cookie.getValue();
                break;
            }
        }

        Token token = tokenRepository.findByToken(refreshTokenRequest);

        if(token!=null){

            String accessToken = generateAccessToken(token.getUser());

                return ResponseEntity.ok().body(new ApiResponse(true, "Token refreshed successfully", accessToken));
        }else{
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Token not found", null));
        }

    }
    return ResponseEntity.badRequest().body(new ApiResponse(false, "Token not found", null));

    }



    @Autowired
    public TokenServiceImpl(TokenRepository tokenRepository, JwtUtil jwtUtil) {
        this.tokenRepository = tokenRepository;
        this.jwtUtil = jwtUtil;
    }


    @Override
    public Token saveToken(String token, User user) {
        return tokenRepository.save(new Token(token,user)) ;
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
}
