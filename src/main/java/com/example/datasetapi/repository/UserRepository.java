package com.example.datasetapi.repository;

import com.example.datasetapi.dto.response.UserLoginData;
import com.example.datasetapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);




    Optional<User> findByEmail(String email);
}
