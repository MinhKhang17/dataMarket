package com.example.datasetapi.service.user;

import com.example.datasetapi.mapper.DatasetMapper;
import com.example.datasetapi.mapper.UserMapper;
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
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.repository.*;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.LoginResponse;

import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.repository.RoleRepository;
import com.example.datasetapi.repository.ProviderRegistrationRepository;
import com.example.datasetapi.service.feature.ImageServiceImpl;
import com.example.datasetapi.repository.WalletRepository;
import com.example.datasetapi.service.payment.PaymentService;
import com.example.datasetapi.service.payment.WalletService;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final WalletService walletService;

    @Autowired
    private  DatasetMapper datasetMapper;
    @Autowired
    private CommuneRepository communeRepository;
    @Autowired
    public UserServiceImpl(ProviderRepository providerRepository, UserRepository userRepository, JwtUtil jwtUtil, TokenServiceImpl tokenService, RoleRepository roleRepository, ImageServiceImpl imageService, ProviderIndentityDocumentRepository providerIdentityDocumentRepository, ProviderRegistrationRepository providerRegistrationRepository, WalletRepository walletRepository, WalletService walletService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.imageService = imageService;
        this.providerIdentityDocumentRepository = providerIdentityDocumentRepository;
        this.providerRegistrationRepository = providerRegistrationRepository;
        this.walletRepository = walletRepository;
        this.providerRepository = providerRepository;
        this.walletService = walletService;
    }


    @Override
    public Provider findProviderById(long providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not exits with id: " + providerId));
    }

    @Override
    public boolean isExitsProvider(long providerId) {
        return providerRepository.existsById(providerId);
    }

    @Override
    public ResponseEntity<?> getProviderCommune(HttpServletRequest request) {
        Provider provider = findProviderById(tokenService.getUserIdFromRequest(request));

        if(provider==null){
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND);
        }
        if(provider.getCommunes() == null || provider.getCommunes().isEmpty()){
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.COMMUNE_NOT_FOUND);
        }
        List<Commune> communes = provider.getCommunes();
        return ResponseEntity.ok().body(new ApiResponse(true,"Load Communes Success",communes.stream().map(datasetMapper::toCommuneDTO).collect(Collectors.toList())));
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
            throw new CustomException(HttpStatus.NOT_FOUND,ErrorCode.USER_NOT_FOUND);
        }

            User user = userOptional.get();

        if(!user.isActive()){
            throw new CustomException(HttpStatus.NOT_FOUND,ErrorCode.USER_NOT_FOUND);
        }


            boolean isValidPassword = PasswordUtil.matches(loginRequest.getPassword(), user.getPassword());
            if (!isValidPassword) {
                throw new CustomException(HttpStatus.NOT_FOUND,ErrorCode.USER_NOT_FOUND);
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
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // check password
        if (registerRequest.getPassword().length() < 8) {
        throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.PASSWORD_TOO_SHORT);
        }
        if (!Validator.isValidPassword(registerRequest.getPassword())) {
        throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.PASSWORD_TOO_WEAK);
        }

        // check email
        if (!Validator.isValidEmail(registerRequest.getEmail())) {
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.EMAIL_INVALID);
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // tạo user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(PasswordUtil.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setActive(true);

        // gán role
        Optional<Role> roleOptional = roleRepository.findByName("CONSUMER");
        if (roleOptional.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.INVALID_ROLE);
        }

        user.setRole(roleOptional.get());
        // lưu user
        try {
            userRepository.save(user);
            walletService.createWallet(user);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Error", e.getMessage()));
        }

        return ResponseEntity.ok().body(new ApiResponse(true, "User registered successfully", user.getUsername()));
    }



    @Transactional
    @Override
    public ResponseEntity<ApiResponse> updatePassword(UpdatePasswordRequest request) {
            // check input
            if (request == null || request.getNewPassword() == null ||
                    request.getOldPassword() == null || request.getConfirmPassword() == null) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.MISSING_REQUIRED_FIELD);
            }
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.PASSWORD_MISMATCH);
            }
            if (request.getNewPassword().length() < 8) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.PASSWORD_TOO_SHORT);
            }
            if (!Validator.isValidPassword(request.getNewPassword())) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.PASSWORD_TOO_WEAK);
            }


            // check user
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOptional = userRepository.findByUsername(currentUsername);
            if (!userOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "User not found", currentUsername));
            }

            // check old password
            if (!PasswordUtil.matches(request.getOldPassword(), userOptional.get().getPassword())) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.OLD_PASSWORD_INCORRECT);
            }

            //update password
            User user = userOptional.get();
            user.setPassword(PasswordUtil.encode(request.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.ok().body(new ApiResponse(true, "Password update successfully", user.getUsername()));

    }


    public User createUserForLoginByGoogleFlow(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        Optional<User> user = userRepository.findByEmail(email);

        //User chua tao tai khoan truoc do
        if (!user.isPresent()) {
            User newUser = new User();
            newUser.setUsername(oAuth2User.getAttribute("name"));
            newUser.setPassword(randomPassword());
            newUser.setEmail(email);
            Role role = roleRepository.findByName("CONSUMER").get();
            newUser.setRole(role);

            newUser.setAuth_id(oAuth2User.getAttribute("sub"));
            newUser.setAuthor(GOOGLEPROVIDER);

            userRepository.save(newUser);
            walletService.createWallet(newUser);
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
    public User findUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));

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


    private String randomPassword() {
        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = PasswordUtil.encode(randomPassword);
        return encodedPassword;
    }

    @Override
    public ResponseEntity<ApiResponse> ProviderRegistrationProcess(ProviderRegistrationRequestDTO providerRegistrationDTO) {

            System.out.println(providerRegistrationDTO.getFullName());
            // Validate input
            if (providerRegistrationDTO == null) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.MISSING_REQUIRED_FIELD);
            }

            // Validate basic fields
            if (providerRegistrationDTO.getFullName() == null || providerRegistrationDTO.getFullName().trim().isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.MISSING_REQUIRED_FIELD);
            }

            if (providerRegistrationDTO.getEmail() == null || providerRegistrationDTO.getEmail().trim().isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.MISSING_REQUIRED_FIELD);
            }

            if (providerRegistrationDTO.getPhoneNumber() == null || providerRegistrationDTO.getPhoneNumber().trim().isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.MISSING_REQUIRED_FIELD);
            }

            if (providerRegistrationDTO.getProvinceId() == null || providerRegistrationDTO.getProvinceId().trim().isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
            }

            if (providerRegistrationDTO.getCommuneId() == null || providerRegistrationDTO.getCommuneId().trim().isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
            }

            // Check if email already exists
            if (providerRegistrationRepository.existsByEmail(providerRegistrationDTO.getEmail())) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            if(providerRegistrationRepository.existsByPhoneNumber(providerRegistrationDTO.getPhoneNumber())) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.PHONE_EXISTS);
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


    }

    @Transactional
    protected ProviderRegistration registerProvider(ProviderRegistrationRequestDTO providerRegistrationDTO) {
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

    private ProviderRegistration createProviderRegistrationFromDTO(ProviderRegistrationRequestDTO dto) {
        ProviderRegistration providerRegistration = new ProviderRegistration();

        providerRegistration.setFullName(dto.getFullName());
        providerRegistration.setEmail(dto.getEmail());
        providerRegistration.setPhoneNumber(dto.getPhoneNumber());
        providerRegistration.setOrganizationName(dto.getOrganizationName());
        providerRegistration.setTaxId(dto.getTaxId());

        providerRegistration.setProvinceId(dto.getProvinceId());
        providerRegistration.setCommune(communeRepository.findById(dto.getCommuneId()).orElse(null));

        providerRegistration.setRegistrationStatus(RegistrationStatus.PENDING);


        return providerRegistration;
    }

    private List<ProviderIdentityDocument> handleProviderDocument(
            ProviderRegistrationRequestDTO providerRegistrationDTO,
            ProviderRegistration providerRegistration) {

        List<ProviderIdentityDocument> providerIdentityDocuments = new ArrayList<>();
        List<ProvierIdentityDocumentDTO> providerDocumentDTOs = providerRegistrationDTO.getIdentityDocuments();

        // Kiểm tra danh sách document có tồn tại không
        if (providerDocumentDTOs == null || providerDocumentDTOs.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
        }

        // Xử lý từng document
        for (ProvierIdentityDocumentDTO dto : providerDocumentDTOs) {
            // Kiểm tra file có tồn tại không
            if (dto.getFile() == null || dto.getFile().isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
            }


            // Validate file type (optional)
            String contentType = dto.getFile().getContentType();
            if (contentType == null || (!contentType.startsWith("image/"))) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
            }

            // Validate file size (optional - 5MB limit)
            if (dto.getFile().getSize() > 5 * 1024 * 1024) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.FILE_TOO_BIG);
            }

        // Tạo ProviderIdentityDocument từ DTO
        ProviderIdentityDocument providerIdentityDocument = new ProviderIdentityDocument();
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
    public Role findRoleByName(String provider) {
        return roleRepository.findByName(provider).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.ROLE_NOT_FOUND));
    }

    @Override
    public Provider saveProvider(Provider provider) {
        return providerRepository.save(provider);
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

