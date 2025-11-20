package com.example.datasetapi.service.user;

import com.example.datasetapi.dto.response.ProviderRegistrationResponseDTO;
import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.model.dataset.PricingRule;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AdminService {
    List<ProviderRegistrationResponseDTO> getProviderRegisPending(RegistrationStatus pending);

    ResponseEntity<?> acceptProviderRegis(long providerRegistrationId, HttpServletRequest request);

    ResponseEntity<?> rejectProviderRegis(long providerRegistrationId, String reason, HttpServletRequest request);

    ResponseEntity<?> getReviewProviderHistory();

    List<PricingRule> getAllPricingRuleForAdmin();

    PricingRule getDetailPricingRule(long id);

    PricingRule updatePricingRule(PricingRule pricingRule);
}
