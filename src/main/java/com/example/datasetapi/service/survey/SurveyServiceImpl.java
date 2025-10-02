package com.example.datasetapi.service.survey;

import com.example.datasetapi.dto.request.ConsumerRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.ConsumerResponse;
import com.example.datasetapi.dto.response.ConsumerTypeResponse;
import com.example.datasetapi.dto.response.TypeOptionResponse;
import com.example.datasetapi.model.userManager.Consumer;
import com.example.datasetapi.model.userManager.ConsumerType;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.ConsumerRepository;
import com.example.datasetapi.repository.ConsumerTypeRepository;
import com.example.datasetapi.repository.UserRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {

    private final ConsumerRepository consumerRepository;
    private final ConsumerTypeRepository consumerTypeRepository;
    private final JwtUtil jwtUtil;
    private final jakarta.servlet.http.HttpServletRequest request;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Override
    public ResponseEntity<ApiResponse> getOptionsForSurvey() {
        try {
            String token = tokenService.resolveToken(request);
            if(token == null) {
                return ResponseEntity.status(401).body(new ApiResponse(false, "Missing/invalid Authorization", null));
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(new ApiResponse(false, "Invalid or expired token", null));
            }

            Consumer consumer = consumerRepository.findById(userId).orElse(null);
            List<Long> selectedTypeIds = (consumer != null && consumer.getConsumerTypes() != null) ?
                    consumer.getConsumerTypes().stream().map(ConsumerType::getId).toList() : List.of();

            var options = consumerTypeRepository.findAll().stream()
                    .map(t -> new TypeOptionResponse(t.getId(), t.getName(), selectedTypeIds.contains(t.getId())))
                    .toList();
            return ResponseEntity.ok(new ApiResponse(true, "OK", options));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse> submitSurveyResponses(ConsumerRequest consumerRequest) {
        try {
            String token = tokenService.resolveToken(request);

            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) return ResponseEntity.status(401)
                    .body(new ApiResponse(false, "Invalid or expired token", null));

            Consumer consumer = consumerRepository.findById(userId).orElseGet(() -> {
                User u = userRepository.findById(userId).orElseThrow();
                Consumer c = new Consumer();
                c.setUser(u);
                c.setConsumerTypes(new ArrayList<>());
                return consumerRepository.save(c);
            });

            List<Long> typeIds = (consumerRequest.getTypeIds() == null)
                    ? new ArrayList<>() : new ArrayList<>(consumerRequest.getTypeIds());

            if (consumerRequest.getOtherType() != null && !consumerRequest.getOtherType().isBlank()) {
                String name = consumerRequest.getOtherType().trim();
                ConsumerType other = consumerTypeRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> {
                            ConsumerType t = new ConsumerType();
                            t.setName(name);
                            return consumerTypeRepository.save(t);
                        });
                typeIds.add(other.getId());
            }

            List<ConsumerType> types = typeIds.isEmpty() ? List.of() : consumerTypeRepository.findAllById(typeIds);
            if (types.size() != typeIds.size())
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Some typeIds are invalid", null));

            consumer.getConsumerTypes().clear();
            consumer.getConsumerTypes().addAll(types);
            consumer.setDoSurvey(true);
            consumerRepository.save(consumer);

            var result = types.stream().map(t -> new ConsumerTypeResponse(t.getId(), t.getName())).toList();
            return ResponseEntity.ok(new ApiResponse(true, "Survey submitted",
                    new ConsumerResponse(consumer.getId(), result, consumer.isDoSurvey())));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
