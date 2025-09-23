package com.example.datasetapi.config.system;

import com.example.datasetapi.model.Role;
import com.example.datasetapi.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {

      roleRepository.save(new Role("ADMIN"));

        roleRepository.save(new Role("USER"));

        System.out.println("Roles & permissions initialized.");
    }
}
