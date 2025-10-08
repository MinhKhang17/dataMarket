package com.example.datasetapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapper {
    @Bean
    public ModelMapper modelMapperConfig() {
        return new ModelMapper();
    }
}
