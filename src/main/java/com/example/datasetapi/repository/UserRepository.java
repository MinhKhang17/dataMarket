package com.example.datasetapi.repository;

import com.example.datasetapi.dto.response.UserLoginData;
import com.example.datasetapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("select new com.example.datasetapi.dto.response.UserLoginData(u.id, u.email, u.password, u.username) " +
            "from User u where u.username like :name")
    Optional<UserLoginData> findBasicInformationByName(@Param("name") String name);

    User findByEmail(String email);
}
