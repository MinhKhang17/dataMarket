package com.example.datasetapi.config.system;

import com.example.datasetapi.model.userManager.ConsumerType;
import com.example.datasetapi.model.userManager.Role;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.ConsumerTypeRepository;
import com.example.datasetapi.repository.RoleRepository;
import com.example.datasetapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ConsumerTypeRepository consumerTypeRepository;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, ConsumerTypeRepository consumerTypeRepository) {
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.consumerTypeRepository = consumerTypeRepository;
    }


    @Override
    public void run(String... args) throws Exception {

        roleRepository.save(new Role("ADMIN"));
        roleRepository.save(new Role("CONSUMER"));

        createConsumerRole();
        createAdminRole();

        createConsumerTypes();

        System.out.println("Seeded roles, users, and consumer types.");
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

    private void createConsumerTypes() {
        ConsumerType t1 = new ConsumerType();
        t1.setName("Student");
        consumerTypeRepository.save(t1);

        ConsumerType t2 = new ConsumerType();
        t2.setName("Researcher");
        consumerTypeRepository.save(t2);

        ConsumerType t3 = new ConsumerType();
        t3.setName("Educator");
        consumerTypeRepository.save(t3);

        ConsumerType t4 = new ConsumerType();
        t4.setName("Startup");
        consumerTypeRepository.save(t4);

        ConsumerType t5 = new ConsumerType();
        t5.setName("Business");
        consumerTypeRepository.save(t5);
    }
}
