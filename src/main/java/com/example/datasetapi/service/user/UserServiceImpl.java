package com.example.datasetapi.service.user;

import com.example.datasetapi.Mapper.UserMapper;
import com.example.datasetapi.dto.request.LoginRequest;
import com.example.datasetapi.dto.request.ProviderRegistrationRequestDTO;
import com.example.datasetapi.dto.request.RegisterRequest;
import com.example.datasetapi.dto.request.UpdatePasswordRequest;
import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.dto.service.ProvierIdentityDocumentDTO;
import com.example.datasetapi.enums.DocumentType;
import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.enums.VerificationStatus.VerificationStatus;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.userManager.*;
import com.example.datasetapi.repository.*;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.LoginResponse;

import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.repository.RoleRepository;
import com.example.datasetapi.repository.ProviderRegistrationRepository;
import com.example.datasetapi.service.feature.ImageServiceImpl;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final String GOOGLEPROVIDER= "GOOGLE";

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    private final TokenService tokenService;

    private final RoleRepository roleRepository;

    private final ImageServiceImpl imageService;
    private final WalletRepository walletRepository;


    private final ProviderIndentityDocumentRepository providerIdentityDocumentRepository;

    private final ProviderRegistrationRepository providerRegistrationRepository;

    private final ProviderRepository providerRepository;
    @Autowired
    public UserServiceImpl(ProviderRepository providerRepository,UserRepository userRepository, JwtUtil jwtUtil, TokenServiceImpl tokenService, RoleRepository roleRepository, ImageServiceImpl imageService, ProviderIndentityDocumentRepository providerIdentityDocumentRepository, ProviderRegistrationRepository providerRegistrationRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.imageService = imageService;
        this.providerIdentityDocumentRepository = providerIdentityDocumentRepository;
        this.providerRegistrationRepository = providerRegistrationRepository;
        this.walletRepository = walletRepository;
        this.providerRepository = providerRepository;
    }


    @Override
    public Provider findProviderById(long providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not exits with id: " + providerId));
    }

    @Override
    public Address findProviderAddressByProviderIdAndAddressId(long providerId,long addressId) {
    Optional<Provider> providerOptional = providerRepository.findById(providerId);
    for(Address address : providerOptional.get().getAddresses()) {
        if(address.getId() == addressId) {
            return address;
        }
    }
return null;
    }


    @Override
    @Transactional
    public ResponseEntity<ApiResponse> login(LoginRequest loginRequest, HttpServletResponse response) {
        Optional<User> userOptional;


        if (Validator.isValidEmail(loginRequest.getUsername())) {
            userOptional = userRepository.findByEmail((loginRequest.getUsername()));
        }
        //lấy user từ repository theo username
        else {
            userOptional = userRepository.findByUsername(loginRequest.getUsername());
        }
        if (!userOptional.isPresent()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

            User user = userOptional.get();

            boolean isValidPassword = PasswordUtil.matches(loginRequest.getPassword(), user.getPassword());
            if (!isValidPassword) {
                throw new CustomException(ErrorCode.USER_NOT_FOUND);
            }

            //tao token
                String accessTokenCreated = tokenHandler(user, response);
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setAccessToken(accessTokenCreated);
                loginResponse.setUserName(user.getUsername());


                return ResponseEntity.ok().body(new ApiResponse(true, "login Success", loginResponse));
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
        addRefreshTokenToCookie(refreshTokenCreated, response);

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
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new CustomException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // check password
        if (registerRequest.getPassword().length() < 8) {
        throw new CustomException(ErrorCode.PASSWORD_TOO_SHORT);
        }
        if (!Validator.isValidPassword(registerRequest.getPassword())) {
        throw new CustomException(ErrorCode.PASSWORD_TOO_WEAK);
        }

        // check email
        if (!Validator.isValidEmail(registerRequest.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_INVALID);
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // tạo user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(PasswordUtil.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());


        // gán role
        Optional<Role> roleOptional = roleRepository.findByName("CONSUMER");
        if (roleOptional.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_ROLE);
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



    @Transactional
    @Override
    public ResponseEntity<ApiResponse> updatePassword(UpdatePasswordRequest request) {
        try {
            // check input
            if (request == null || request.getNewPassword() == null ||
                    request.getOldPassword() == null || request.getConfirmPassword() == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Missing input", null));
            }
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "New password and confirm password do not match", null));
            }
            if (request.getNewPassword().length() < 8) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "New password too short", null));
            }
            if (!Validator.isValidPassword(request.getNewPassword())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid new password", null));
            }

            // check user
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOptional = userRepository.findByUsername(currentUsername);
            if (!userOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "User not found", currentUsername));
            }

            // check old password
            if (!PasswordUtil.matches(request.getOldPassword(), userOptional.get().getPassword())) {
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


    public User createUserForLoginByGoogleFlow(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        Optional<User> user = userRepository.findByEmail(email);

        //User chua tao tai khoan truoc do
        if (!user.isPresent()) {
            User newUser = new User();
            newUser.setUsername(oAuth2User.getAttribute("name"));
            newUser.setPassword(randowPassword());
            newUser.setEmail(email);
            Role role = roleRepository.findByName("CONSUMER").get();
            newUser.setRole(role);

            newUser.setAuth_id(oAuth2User.getAttribute("sub"));
            newUser.setAuthor(GOOGLEPROVIDER);

            userRepository.save(newUser);
            return newUser;
        }


        return user.get();
    }

    @Override
    public ResponseEntity<ApiResponse> logout(HttpServletResponse response, HttpServletRequest request) {

        String token = tokenService.resolveToken(request);

        long userId = jwtUtil.getUserIdFromToken(token);

        tokenService.deleteByUserId(userId);


        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);



        return ResponseEntity.ok().body(new ApiResponse(true, "refresh Success",null));
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

    @Override
    public ResponseEntity<ApiResponse> ProviderRegistrationProcess(ProviderRegistrationRequestDTO providerRegistrationDTO) {
        try {
            System.out.println(providerRegistrationDTO.getFullName());
            // Validate input
            if (providerRegistrationDTO == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Registration data is required", null));
            }

            // Validate basic fields
            if (providerRegistrationDTO.getFullName() == null || providerRegistrationDTO.getFullName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Full name is required", null));
            }

            if (providerRegistrationDTO.getEmail() == null || providerRegistrationDTO.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Email is required", null));
            }

            if (providerRegistrationDTO.getPhoneNumber() == null || providerRegistrationDTO.getPhoneNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Phone number is required", null));
            }

            // Check if email already exists
            if (providerRegistrationRepository.existsByEmail(providerRegistrationDTO.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Email already exists in the system", null));
            }

            // Process registration using the existing registerProvider method
            ProviderRegistration savedRegistration = registerProvider(providerRegistrationDTO);

            // Create response data
            ProviderRegistrationResponseDTO responseDTO = new ProviderRegistrationResponseDTO();
            responseDTO.setId(savedRegistration.getId());
            responseDTO.setFullName(savedRegistration.getFullName());
            responseDTO.setEmail(savedRegistration.getEmail());
            responseDTO.setPhoneNumber(savedRegistration.getPhoneNumber());
            responseDTO.setRegistrationStatus(savedRegistration.getRegistrationStatus().toString());
            responseDTO.setCreatedAt(LocalDateTime.now());

            return ResponseEntity.ok(new ApiResponse(true,
                    "Provider registration submitted successfully. Your application is under review.",
                    responseDTO));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error uploading documents: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An unexpected error occurred during registration", null));
        }
    }

    @Transactional
    protected ProviderRegistration registerProvider(ProviderRegistrationRequestDTO providerRegistrationDTO) throws IOException {
        // Tạo ProviderRegistration từ DTO
        ProviderRegistration providerRegistration = createProviderRegistrationFromDTO(providerRegistrationDTO);

        // Lưu ProviderRegistration trước để có ID
        providerRegistration = providerRegistrationRepository.save(providerRegistration);

        // Xử lý documents
        List<ProviderIdentityDocument> identityDocuments = handleProviderDocument(providerRegistrationDTO, providerRegistration);

        // Set danh sách documents cho provider registration
        providerRegistration.setIdentityDocuments(identityDocuments);

        // Lưu lại để update relationship
        return providerRegistrationRepository.save(providerRegistration);
    }

    private ProviderRegistration createProviderRegistrationFromDTO(ProviderRegistrationRequestDTO providerRegistrationDTO) {
        ProviderRegistration providerRegistration = new ProviderRegistration();

        // Set thông tin cơ bản
        providerRegistration.setFullName(providerRegistrationDTO.getFullName());
        providerRegistration.setEmail(providerRegistrationDTO.getEmail());
        providerRegistration.setPhoneNumber(providerRegistrationDTO.getPhoneNumber());

        // Set thông tin địa chỉ (nếu có trong DTO - cần thêm vào DTO)
        // providerRegistration.setAddressLine(providerRegistrationDTO.getAddressLine());
        // providerRegistration.setCity(providerRegistrationDTO.getCity());
        // providerRegistration.setDistrict(providerRegistrationDTO.getDistrict());
        // providerRegistration.setWard(providerRegistrationDTO.getWard());

        // Set trạng thái và thời gian
        providerRegistration.setRegistrationStatus(RegistrationStatus.PENDING);
        providerRegistration.setCreatedAt(Instant.now());
        providerRegistration.setUpdatedAt(Instant.now());

        return providerRegistration;
    }

    private List<ProviderIdentityDocument> handleProviderDocument(
            ProviderRegistrationRequestDTO providerRegistrationDTO,
            ProviderRegistration providerRegistration) throws IOException {

        List<ProviderIdentityDocument> providerIdentityDocuments = new ArrayList<>();
        List<ProvierIdentityDocumentDTO> providerDocumentDTOs = providerRegistrationDTO.getIdentityDocuments();

        // Kiểm tra danh sách document có tồn tại không
        if (providerDocumentDTOs == null || providerDocumentDTOs.isEmpty()) {
            throw new IllegalArgumentException("The provider must submit all required identification documents");
        }

        // Xử lý từng document
        for (ProvierIdentityDocumentDTO dto : providerDocumentDTOs) {
            // Kiểm tra file có tồn tại không
            if (dto.getFile() == null || dto.getFile().isEmpty()) {
                throw new IllegalArgumentException("Each identification document must include a valid image file");
            }


            // Validate file type (optional)
            String contentType = dto.getFile().getContentType();
            if (contentType == null || (!contentType.startsWith("image/"))) {
                throw new IllegalArgumentException("Only image files are allowed for identification documents");
            }

            // Validate file size (optional - 5MB limit)
            if (dto.getFile().getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("File size must not exceed 5MB");
            }
            // Tạo ProviderIdentityDocument từ DTO
            ProviderIdentityDocument providerIdentityDocument = new ProviderIdentityDocument();
            providerIdentityDocument.setProvider(providerRegistration);
            providerIdentityDocument.setUploadedAt(Instant.now());
            providerIdentityDocument.setIdCardVerificationStatus(VerificationStatus.PENDING);

            // Upload image và set URL
//            String imageUrl = imageService.uploadImage(dto.getFile());
//            providerIdentityDocument.setImage_url(imageUrl);

            // Set document type if available in DTO
             providerIdentityDocument.setDocumentType(DocumentType.valueOf(dto.getType()));

            // Thêm vào danh sách và lưu vào database
            providerIdentityDocuments.add(providerIdentityDocument);

            providerIdentityDocumentRepository.save(providerIdentityDocument);
        }

        return providerIdentityDocuments;
    }


    @Override
    public ResponseEntity<ApiResponse> getUserInformationFromRequest(HttpServletRequest request) {
        try {
            long userIdFromRequest = jwtUtil.getUserIdFromToken(tokenService.resolveToken(request));


            Optional<User> userOptional = userRepository.findById(userIdFromRequest);
            if (!userOptional.isPresent()) {
                throw new Exception("User not found");
            }
            User user = userOptional.get();

            UserInformationResponseForAuthMe userResponse = UserMapper.toUserInformationResponseForAuthMeDTO(user);

            return ResponseEntity.ok().body(new ApiResponse(true, "User Information", userResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Error", e.getMessage()));
        }
    }
}

