package com.example.datasetapi.service.user;

import com.example.datasetapi.mapper.UserMapper;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.ProviderRegistrationResponseDTO;
import com.example.datasetapi.enums.UserStatus;
import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.userManager.*;
import com.example.datasetapi.repository.ProviderRegisReviewHistoryRepo;
import com.example.datasetapi.repository.ProviderRegistrationRepository;
import com.example.datasetapi.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    private ProviderRegistrationRepository providerRegistrationRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;
@Autowired
    private ProviderRegisReviewHistoryRepo providerRegisReviewHistoryRepo;

    @Override
    public List<ProviderRegistrationResponseDTO> getProviderRegisPending(RegistrationStatus status) {
        return providerRegistrationRepository.findByRegistrationStatus(status)
                .stream()
                .sorted(Comparator.comparing(ProviderRegistration::getCreatedAt))
                .limit(5)
                .map(userMapper::toProviderRegisRepsonseDTO)
                .collect(Collectors.toList()) ;
    }

    @Override
    public ResponseEntity<?> acceptProviderRegis(long providerRegistrationId, HttpServletRequest request)
    {
            Optional<ProviderRegistration> providerRegistrationOptional = providerRegistrationRepository.findById(providerRegistrationId);

            if(providerRegistrationOptional.isEmpty()){
                throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.ProviderRegistrationNotFound);
            }

            User admin = userService.findUserById(tokenService.getUserIdFromRequest(request));

            ProviderRegistration providerRegistration = providerRegistrationOptional.get();
            providerRegistration.setRegistrationStatus(RegistrationStatus.APPROVED);
            providerRegistration.setUpdatedAt(LocalDateTime.now());

            AtomicReference<String> tempUserName = new AtomicReference<>("");
            AtomicReference<String> tempPassword = new AtomicReference<>("");

            Provider provider = createProviderAccount(providerRegistration,tempUserName,tempPassword);

            sendMail(tempPassword.get(),tempUserName.get(), "");

            providerRegistration.setProvider(provider);

            ProviderRegisReviewHistory  providerRegisReviewHistory = new ProviderRegisReviewHistory(admin,
                    provider,true);

            providerRegistrationRepository.save(providerRegistration);
            providerRegisReviewHistoryRepo.save(providerRegisReviewHistory);

            return ResponseEntity.ok().body(new ApiResponse(true,"Accept Success Account will send to email username: "+tempUserName.get()+" Password= "+tempPassword.get(),providerRegistration.getEmail()));
    }

    @Override
    public ResponseEntity<?> rejectProviderRegis(long providerRegistrationId, String reason, HttpServletRequest request) {
        Optional<ProviderRegistration> providerRegistrationOptional = providerRegistrationRepository.findById(providerRegistrationId);

        User admin = userService.findUserById(tokenService.getUserIdFromRequest(request));

        if(providerRegistrationOptional.isEmpty()){
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.ProviderRegistrationNotFound);
        }
        ProviderRegistration providerRegistration = providerRegistrationOptional.get();
        providerRegistration.setRegistrationStatus(RegistrationStatus.REJECTED);
        providerRegistration.setUpdatedAt(LocalDateTime.now());

        ProviderRegisReviewHistory providerRegisReviewHistory = new ProviderRegisReviewHistory(admin,reason,false);
        sendMail("","",reason);
        providerRegistrationRepository.save(providerRegistration);
        providerRegisReviewHistoryRepo.save(providerRegisReviewHistory);
        return ResponseEntity.ok().body(new ApiResponse(true,"Reject success with reason will send to email",providerRegistration.getEmail()));
    }

    @Override
    public ResponseEntity<?> getReviewProviderHistory() {
        List<ProviderRegisReviewHistory> listReview = providerRegisReviewHistoryRepo.findAll();
        return ResponseEntity.ok().body(listReview.stream().map(userMapper::toProviderRegisReviewHistory).collect(Collectors.toList()));
    }

    private void sendMail(String tempPassword, String tempUserName, String reason) {
        System.out.println("sendMail");
        System.out.println("tempPassword:"+tempPassword);
        System.out.println("tempUserName:"+tempUserName);
        System.out.println("reason:"+reason);
    }

    private Provider createProviderAccount(ProviderRegistration providerRegistration, AtomicReference<String> tempUserName, AtomicReference<String> tempPassword) {
        User user = new User();
        user.setUserStatus(UserStatus.ACTIVE);
        user.setEmail(providerRegistration.getEmail());
        user.setUsername(user.getEmail());
        user.setRole(userService.findRoleByName("PROVIDER"));
        user.setActive(true);
        String password = UUID.randomUUID().toString();
        user.setPassword(PasswordUtil.encode(password));
        Provider provider = new Provider();
        provider.setUser(user);
        provider.setCommunes(List.of(providerRegistration.getCommune()));
        tempUserName.set(user.getUsername());
        tempPassword.set(password);
        return userService.saveProvider(provider);
    }
}
