package com.example.datasetapi.repository;

import com.example.datasetapi.model.userManager.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRepository extends JpaRepository<Provider,Long> {
}
