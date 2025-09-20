package com.example.datasetapi.controller;

import com.example.datasetapi.config.JwtUtil;
import com.example.datasetapi.dto.response.RegisterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {
    @Autowired
   private JwtUtil jwtUtil ;

    @GetMapping("/test/token")
    public ResponseEntity<RegisterResponse> testToken() {
        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setAccess_token(jwtUtil.generateAccessToken("Khang"));
        registerResponse.setRefresh_token(jwtUtil.generateRefreshToken("Khang"));
        return ResponseEntity.ok(registerResponse);
    }
}
