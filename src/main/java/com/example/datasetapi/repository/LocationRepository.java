package com.example.datasetapi.repository;

import com.example.datasetapi.model.location.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
