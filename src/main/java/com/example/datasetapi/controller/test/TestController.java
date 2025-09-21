package com.example.datasetapi.controller.test;

import com.example.datasetapi.util.JwtUtil;
import com.example.datasetapi.dto.response.RegisterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {
    @Autowired
   private JwtUtil jwtUtil ;

    @GetMapping("/test/token")
    public ResponseEntity<RegisterResponse> testToken() {
        RegisterResponse registerResponse = new RegisterResponse();
//        registerResponse.setAccess_token(jwtUtil.generateAccessToken("Khang"));
        registerResponse.setRefresh_token(jwtUtil.generateRefreshToken("Khang"));
        return ResponseEntity.ok(registerResponse);
    }
}
