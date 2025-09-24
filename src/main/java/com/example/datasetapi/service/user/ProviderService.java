package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.request.ProviderRegistrationRequestDTO;
import com.example.datasetapi.model.UserManager.ProviderRegistration;
import org.springframework.stereotype.Service;

@Service
public interface ProviderService {
    public ProviderRegistration createProviderRegistration(ProviderRegistrationRequestDTO providerRegistrationDTO);
}
