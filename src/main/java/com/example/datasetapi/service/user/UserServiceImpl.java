package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.request.LoginRequest;
import com.example.datasetapi.dto.request.RegisterRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.LoginResponse;
import com.example.datasetapi.model.Role;
import com.example.datasetapi.model.User;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.repository.RoleRepository;
import com.example.datasetapi.repository.WalletRepository;
import com.example.datasetapi.util.PasswordUtil;
import com.example.datasetapi.util.Validator;
import com.example.datasetapi.repository.UserRepository;
import com.example.datasetapi.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final String GOOGLEPROVIDER= "GOOGLE";

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    private final TokenServiceImpl tokenService;

    private final RoleRepository roleRepository;

    private final WalletRepository walletRepository;



    @Autowired
    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, TokenServiceImpl tokenService, RoleRepository roleRepository, WalletRepository walletRepository, WalletRepository walletRepository1) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.walletRepository = walletRepository;
    }


    @Override
    @Transactional
    public ResponseEntity<ApiResponse> login(LoginRequest loginRequest, HttpServletResponse response) {
        Optional<User> userOptional;
        if(Validator.isValidEmail(loginRequest.getUsername())){
             userOptional = userRepository.findByEmail((loginRequest.getUsername()));
        }
        //lấy user từ repository theo username
        else {
            userOptional = userRepository.findByUsername(loginRequest.getUsername());
        }


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

    public void addRefreshTokenToCookie(String refreshTokenCreated, HttpServletResponse response) {

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
        if( userRepository.existsByEmail(registerRequest.getEmail())){
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email is exited", registerRequest.getEmail()));
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

        user.setRole(roleOptional.get());
        // lưu user
        try {
            userRepository.save(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Error", e.getMessage()));
        }

        return ResponseEntity.ok().body(new ApiResponse(true, "User registered successfully", user.getUsername()));
    }

    @Override
    public User createUserForLoginByGoogleFlow(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        Optional<User> user = userRepository.findByEmail(email);

        //User chua tao tai khoan truoc do
        if(!user.isPresent() ) {
            User newUser = new User();
            newUser.setUsername(oAuth2User.getAttribute("name"));
            newUser.setPassword(randowPassword());
            newUser.setEmail(email);
            Role role = roleRepository.findByName("USER").get();
            newUser.setRole(role);
            newUser.setProvider_id(oAuth2User.getAttribute("sub"));
            newUser.setProvider(GOOGLEPROVIDER);
            userRepository.save(newUser);
            return newUser;
        }


        return user.get();
    }

    @Override
    public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {

//        tokenService.deleteByToken(response.);

        Cookie cookie = new  Cookie("refresh_token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok().body(new ApiResponse(true, "refresh Success", cookie));
    }

    @Override
    public Optional<User> findUserById(long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public ResponseEntity<?> getWalletAmountFromToken(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(tokenService.resolveToken(request));

            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));

            return ResponseEntity.ok(
                    new ApiResponse(true, "Wallet Amount Available", wallet.getAmount())
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Invalid token or user not found", null)
            );
        }
    }


    private String randowPassword() {
        String randowPassword = UUID.randomUUID().toString();
        String encodedPassword = PasswordUtil.encode(randowPassword);
        return encodedPassword;
    }
}
