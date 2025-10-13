package com.example.datasetapi.dto.response;

import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.userManager.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewHistoryDto {

        private long reviewId;

        private ProviderDto providerDto;

        private DatasetDTO datasetDTO;

        private UserDto moderator;

        private UserDto admin;

        private LocalDateTime createAt =  LocalDateTime.now();

        private String reason;

    }

