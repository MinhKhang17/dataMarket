//package com.example.datasetapi.controller.authentication;
//
//import com.example.datasetapi.dto.response.ApiResponse;
//import com.example.datasetapi.service.user.TokenService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("api/auth/refresh")
//public class TokenController {
//    private TokenService tokenService;
//
//    @Autowired
//    public TokenController(TokenService tokenService) {
//        this.tokenService = tokenService;
//    }
//
//    @GetMapping()
//    public ResponseEntity<ApiResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
//        return tokenService.refrestAccessToken(request,response);
//    }
//
//
//
//}
