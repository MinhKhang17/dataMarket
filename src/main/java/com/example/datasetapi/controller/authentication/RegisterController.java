package com.example.datasetapi.controller.authentication;

import com.example.datasetapi.dto.request.RegisterRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("api/auth/register")
public class RegisterController {
    @Value("${app.frontend-url}")
    private String frontendLoginUrl;


    private final UserService userService;
    @Autowired private TokenService tokenService;
    @Autowired
    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest registerRequest) {
        System.out.println(registerRequest.getEmail());
        return userService.register(registerRequest);
    }

    @GetMapping("verify-email")
    public void validateEmail(@RequestParam("token") String token,
                              HttpServletResponse response) throws IOException {

        tokenService.verifyEmailToken(token);

        // Redirect về trang login của React + gắn flag verified
        response.sendRedirect(frontendLoginUrl);
    }


}
