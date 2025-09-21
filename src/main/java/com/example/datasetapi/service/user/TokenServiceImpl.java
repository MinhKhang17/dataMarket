package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.response.UserLoginData;
import com.example.datasetapi.model.Token;
import com.example.datasetapi.model.User;
import com.example.datasetapi.repository.TokenRepository;
import com.example.datasetapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService{

    private TokenRepository tokenRepository;

    private JwtUtil jwtUtil;

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
    public String generateAccessToken(UserLoginData token) {
        return jwtUtil.generateAccessToken(token);
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

        // Or use JPA reference if your Token entity supports it
        // User userReference = userRepository.getReferenceById(userId);
        // token.setUser(userReference);

        tokenRepository.save(token);
    }
}
