package com.example.datasetapi.config;

import com.example.datasetapi.model.UserManager.User;
import com.example.datasetapi.service.user.TokenServiceImpl;
import com.example.datasetapi.service.user.UserServiceImpl;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    private UserServiceImpl userService;

    private TokenServiceImpl tokenService;
   @Value("${frontEndUrl}")
    private  String FRONTEND_URL;

    @Autowired
    public void setUserService(UserServiceImpl userService, TokenServiceImpl tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Lấy email từ Google
        String email = oAuth2User.getAttribute("email");


        User user = userService.createUserForLoginByGoogleFlow(oAuth2User);

        // Generate JWT access token
        String token = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user.getUsername());

        //them refresh token vao cookies
        userService.addRefreshTokenToCookie(refreshToken, response);
        //luu vao db
        tokenService.saveToken(refreshToken,user);

        // Trả về JSON cho client
        response.setContentType("application/json");
        response.sendRedirect("http://localhost:5173/oauth2/callback?token="+token);
    }

}