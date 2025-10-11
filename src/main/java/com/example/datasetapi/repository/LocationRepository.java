package com.example.datasetapi.repository;


import com.example.datasetapi.model.userManager.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
