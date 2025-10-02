package com.example.datasetapi.config.system;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class EnvLoader {

    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        // Load biến môi trường vào System, Spring Boot sẽ tự bind
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }
}
