package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.request.ProviderRegistrationRequestDTO;
import com.example.datasetapi.dto.response.ProviderRegistrationResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AdminService {
    List<ProviderRegistrationResponseDTO> getProviderRegisPending();
}
