package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.request.LoginRequest;
import com.example.datasetapi.dto.request.RegisterRequest;
import com.example.datasetapi.dto.request.UpdatePasswordRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.LoginResponse;
import com.example.datasetapi.dto.response.UserLoginData;
import com.example.datasetapi.model.Role;
import com.example.datasetapi.model.User;
import com.example.datasetapi.repository.RoleRepository;
import com.example.datasetapi.util.PasswordUtil;
import com.example.datasetapi.util.Validator;
import com.example.datasetapi.repository.UserRepository;
import com.example.datasetapi.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {


    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    private final TokenServiceImpl tokenService;

    private final RoleRepository roleRepository;


    @Autowired
    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, TokenServiceImpl tokenService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
    }


    @Override
    @Transactional
    public ResponseEntity<ApiResponse> login(LoginRequest loginRequest, HttpServletResponse response) {

        //lấy user từ repository theo username
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());




        if (userOptional.isPresent()) {
            User user = userOptional.get();

            boolean isValidPassword = PasswordUtil.matches(loginRequest.getPassword(), user.getPassword());
            if (isValidPassword) {

                //tao token
                String accessTokenCreated = tokenHandler(user,response);
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setAccessToken(accessTokenCreated);
                loginResponse.setUserName(user.getUsername());


                return ResponseEntity.ok().body(new ApiResponse(true, "login Success", loginResponse));


            }
            else {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid password", null));
            }
        } else {

            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid username or password", null));
        }

    }

    @Transactional
    public String tokenHandler(User user, HttpServletResponse response) {
        //tao accesstoken
        String accessTokenCreated = tokenService.generateAccessToken(user);
        //tao refreshToken
        String refreshTokenCreated = tokenService.generateRefreshToken(String.valueOf(user.getId()));

        //luu vao db
        User userReference = userRepository.getReferenceById(user.getId());
        tokenService.saveToken(refreshTokenCreated, userReference);
        //add vao cookies
        addRefreshTokenToCookie(refreshTokenCreated,response);

        //tra ve access Token
        return accessTokenCreated;
    }

    private void addRefreshTokenToCookie(String refreshTokenCreated, HttpServletResponse response) {

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshTokenCreated);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(3600);
        response.addCookie(refreshTokenCookie);

    }


    @Override
    @Transactional

    public ResponseEntity<ApiResponse> register(RegisterRequest registerRequest) {
        // check username
        if(userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Username already exists", registerRequest.getUsername()));
        }

        // check password
        if(registerRequest.getPassword().length() < 8) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Password too short", registerRequest.getUsername()));
        }
        if(!Validator.isValidPassword(registerRequest.getPassword())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid password", registerRequest.getUsername()));
        }

        // check email
        if(!Validator.isValidEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid email", registerRequest.getEmail()));
        }

        // tạo user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(PasswordUtil.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());

        // gán role
        Optional<Role> roleOptional = roleRepository.findByName("USER");
        if (!roleOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid role", registerRequest.getUsername()));
        }
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(roleOptional.get());
        // lưu user
        try {
            userRepository.save(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Error", e.getMessage()));
        }

        return ResponseEntity.ok().body(new ApiResponse(true, "User registered successfully", user.getUsername()));
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse> updatePassword(UpdatePasswordRequest request) {
        try {
            // check input
            if(request == null || request.getNewPassword() == null ||
                    request.getOldPassword() == null || request.getConfirmPassword() == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Missing input", null));
            }
            if(!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "New password and confirm password do not match", null));
            }
            if(request.getNewPassword().length() < 8) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "New password too short", null));
            }
            if(!Validator.isValidPassword(request.getNewPassword())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid new password", null));
            }

            // check user
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOptional = userRepository.findByUsername(currentUsername);
            if(!userOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "User not found", currentUsername));
            }

            // check old password
            if(!PasswordUtil.matches(request.getOldPassword(), userOptional.get().getPassword())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Old password is incorrect", null));
            }

            //update password
            User user = userOptional.get();
            user.setPassword(PasswordUtil.encode(request.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.ok().body(new ApiResponse(true, "Password update successfully", user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Error", e.getMessage()));
        }
    }

}
