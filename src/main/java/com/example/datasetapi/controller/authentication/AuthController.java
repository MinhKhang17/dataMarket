package com.example.datasetapi.controller.authentication;

import com.example.datasetapi.dto.request.ProviderRegistrationRequestDTO;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("provider/sendFormRegister")
    public ResponseEntity<ApiResponse> sendFormRegister(ProviderRegistrationRequestDTO providerRegistrationDTO) {
        return userService.ProviderRegistratiopnProcess(providerRegistrationDTO);
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> GetUserInformationFromRequest(HttpServletRequest request) {
            return userService.getUserInformationFromRequest(request);
    }

}
