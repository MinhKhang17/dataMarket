package com.example.datasetapi.dto.response;

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

