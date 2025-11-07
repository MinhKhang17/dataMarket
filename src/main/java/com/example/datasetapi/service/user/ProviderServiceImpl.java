package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.ProviderResponse;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.ProviderIdentityDocument;
import com.example.datasetapi.model.userManager.ProviderRegistration;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.ProviderRepository;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.function.EntityResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {
    private final ProviderRepository providerRepository;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;
    private final UserService userService;

    @Override
    public ResponseEntity<ApiResponse> viewProfile() {
        String token = tokenService.resolveToken(request);
        if (token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }
        Long providerId = jwtUtil.getUserIdFromToken(token);
        if(providerId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }
        User user = userService.findUserById(providerId);

        if(!user.getRole().getName().equalsIgnoreCase("Provider")) {
            throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
        }

        Provider provider = providerRepository.findById(providerId).orElseThrow(()
                -> new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.USER_NOT_FOUND));

        ProviderRegistration p = provider.getProviderRegistration();

        List<String> imageUrls = p.getIdentityDocuments() != null ?
                p.getIdentityDocuments().stream()
                .map(ProviderIdentityDocument::getImage_url)
                        .toList()
                : List.of();

        String location = p.getCommune().getName() + "," + p.getCommune().getProvince().getName();

        ProviderResponse response = new ProviderResponse(providerId,
                provider.getProviderRegistration().getFullName(),
                provider.getProviderRegistration().getEmail(),
                provider.getProviderRegistration().getOrganizationName(),
                provider.getProviderRegistration().getPhoneNumber(),
                provider.getProviderRegistration().getTaxId(),
                location, imageUrls);
        return ResponseEntity.ok().body(new ApiResponse(true, "Success", response));
    }
}
