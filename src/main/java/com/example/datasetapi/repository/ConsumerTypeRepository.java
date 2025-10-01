package com.example.datasetapi.repository;

import com.example.datasetapi.model.userManager.ConsumerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsumerTypeRepository extends JpaRepository<ConsumerType, Long> {

}
