package com.example.datasetapi.repository;

import com.example.datasetapi.model.location.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, String> {
    Optional<Object> findByName(String communeName);
}
