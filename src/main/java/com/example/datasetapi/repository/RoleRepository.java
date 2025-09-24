package com.example.datasetapi.repository;

import com.example.datasetapi.model.UserManager.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String roleName);


    Set<Role> findRoleById(Long id);
}
