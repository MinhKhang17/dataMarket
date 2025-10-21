package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.response.ConsumerSubResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ConsumerService {
    List<ConsumerSubResponseDTO> getConsumerSubscriptions(HttpServletRequest request);
}
