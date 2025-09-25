package com.example.datasetapi.repository;

import com.example.datasetapi.dto.service.ProvierIdentityDocumentDTO;
import com.example.datasetapi.model.UserManager.ProviderRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRegistrationRepository extends JpaRepository<ProviderRegistration, Integer> {
    boolean existsByEmail(String email);
}
