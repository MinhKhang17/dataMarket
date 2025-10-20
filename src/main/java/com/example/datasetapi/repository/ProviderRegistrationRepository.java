package com.example.datasetapi.repository;

import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.model.userManager.ProviderRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderRegistrationRepository extends JpaRepository<ProviderRegistration, Long> {
    boolean existsByEmail(String email);

    List<ProviderRegistration> findByRegistrationStatus(RegistrationStatus registrationStatus);

    boolean existsByPhoneNumber(String phoneNumber);
}
