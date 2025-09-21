package com.example.datasetapi.controller.authentication;

import com.example.datasetapi.dto.request.LoginRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.service.user.UserServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth/login")
public class loginController {
    private UserServiceImpl userService;
    @Autowired
    public void LoginController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return  userService.login(loginRequest,response);
    }
}
