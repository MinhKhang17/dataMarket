package com.example.datasetapi.controller.authentication;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private UserService userService;

    public  AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> GetUserInformationFromRequest(HttpServletRequest request) {
            return userService.getUserInformationFromRequest(request);
    }
}
