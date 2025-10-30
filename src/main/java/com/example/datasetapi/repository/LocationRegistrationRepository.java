package com.example.datasetapi.repository;

import com.example.datasetapi.model.location.LocationRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRegistrationRepository extends JpaRepository<LocationRegistration, Long> {
}
