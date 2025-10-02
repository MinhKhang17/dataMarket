package com.example.datasetapi.repository;

import com.example.datasetapi.model.userManager.ProviderRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRegistrationRepository extends JpaRepository<ProviderRegistration, Long> {
    boolean existsByEmail(String email);
}
