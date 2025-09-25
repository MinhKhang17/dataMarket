package com.example.datasetapi.config.system;

import com.example.datasetapi.model.UserManager.Role;
import com.example.datasetapi.model.UserManager.User;
import com.example.datasetapi.repository.RoleRepository;
import com.example.datasetapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
private final UserRepository userRepository;
    public DataInitializer(UserRepository userRepository,RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }


    @Override
    public void run(String... args) throws Exception {

      roleRepository.save(new Role("ADMIN"));

        roleRepository.save(new Role("CONSUMER"));

        System.out.println("Roles & permissions initialized.");
        createConsumerRole();
        createAdminRole();
    }

    private void createAdminRole() {
        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(roleRepository.findByName("ADMIN").get());
        userRepository.save(user);
    }

    private void  createConsumerRole() {
        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail("consumer@gmail.com");
        user.setRole(roleRepository.findByName("CONSUMER").get());
        userRepository.save(user);
    }
}
