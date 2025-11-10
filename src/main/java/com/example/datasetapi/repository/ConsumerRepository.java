package com.example.datasetapi.repository;

import com.example.datasetapi.model.userManager.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsumerRepository extends  JpaRepository<Consumer, Long> {
}
