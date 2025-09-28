package com.example.datasetapi.repository;

import com.example.datasetapi.model.UserManager.Token;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token,Integer> {

    boolean existsTokenByToken(String token);

    Token findByToken(String token);

    @Transactional
    void deleteByToken(String token);

    @Modifying
    @Transactional
    void deleteByUser_Id(Long userId);
    boolean existsByToken(String token);
}
