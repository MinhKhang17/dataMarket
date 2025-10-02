package com.example.datasetapi.controller.authentication;

import com.example.datasetapi.dto.request.UpdatePasswordRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.user.UserServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth/password")
@SecurityRequirement(name = "bearerAuth")
public class PasswordController {
    private final UserServiceImpl userService;

    @Autowired
    public PasswordController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/update-password")
    public ResponseEntity<ApiResponse> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        return userService.updatePassword(updatePasswordRequest);
    }
}
