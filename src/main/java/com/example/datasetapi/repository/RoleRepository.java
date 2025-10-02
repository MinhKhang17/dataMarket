package com.example.datasetapi.repository;

import com.example.datasetapi.model.userManager.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String roleName);


    Set<Role> findRoleById(Long id);

    Role getRolesByName(String name);
}
