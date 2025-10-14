package com.example.datasetapi.repository;

import com.example.datasetapi.model.location.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, String> {
    List<Commune> findByProvince_IdProvince(String idProvince);
}
