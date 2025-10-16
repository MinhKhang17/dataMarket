package com.example.datasetapi.service.user;

import com.example.datasetapi.Mapper.UserMapper;
import com.example.datasetapi.dto.response.ProviderRegistrationResponseDTO;
import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.model.UserManager.ProviderRegistration;
import com.example.datasetapi.repository.ProviderRegistrationRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    private ProviderRegistrationRepository providerRegistrationRepository;
    @Override
    public List<ProviderRegistrationResponseDTO> getProviderRegisPending() {
        return providerRegistrationRepository.findByRegistrationStatus(RegistrationStatus.PENDING)
                .stream()
                .sorted(Comparator.comparing(ProviderRegistration::getCreatedAt))
                .limit(5)
                .map(userMapper::toProviderRegisRepsonseDTO)
                .collect(Collectors.toList()) ;
    }
}
