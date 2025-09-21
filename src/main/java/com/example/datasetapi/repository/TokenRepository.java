package com.example.datasetapi.repository;

import com.example.datasetapi.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token,Long> {

    boolean existsTokenByToken(String token);

    Token findByToken(String token);
}
