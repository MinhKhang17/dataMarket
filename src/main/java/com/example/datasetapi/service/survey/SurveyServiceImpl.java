package com.example.datasetapi.service.survey;

import com.example.datasetapi.dto.request.ConsumerRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.ConsumerResponse;
import com.example.datasetapi.dto.response.ConsumerTypeResponse;
import com.example.datasetapi.dto.response.TypeOptionResponse;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.UserManager.Consumer;
import com.example.datasetapi.model.UserManager.ConsumerType;
import com.example.datasetapi.model.UserManager.User;
import com.example.datasetapi.repository.ConsumerRepository;
import com.example.datasetapi.repository.ConsumerTypeRepository;
import com.example.datasetapi.repository.UserRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            String token = tokenService.resolveToken(request);
            if(token == null) {
                throw new CustomException(HttpStatus.UNAUTHORIZED,ErrorCode.UNAUTHORIZED);
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new CustomException(HttpStatus.UNAUTHORIZED,ErrorCode.INVALID_TOKEN);
            }

            Consumer consumer = consumerRepository.findById(userId).orElse(null);
            List<Long> selectedTypeIds = (consumer != null && consumer.getConsumerTypes() != null) ?
                    consumer.getConsumerTypes().stream().map(ConsumerType::getId).toList() : List.of();

            var options = consumerTypeRepository.findAll().stream()
                    .map(t -> new TypeOptionResponse(t.getId(), t.getName(), selectedTypeIds.contains(t.getId())))
                    .toList();
            return ResponseEntity.ok(new ApiResponse(true, "OK", options));
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse> submitSurveyResponses(ConsumerRequest consumerRequest) {
            String token = tokenService.resolveToken(request);
            if(token == null) {
                throw new CustomException(HttpStatus.UNAUTHORIZED,ErrorCode.UNAUTHORIZED);
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new CustomException(HttpStatus.UNAUTHORIZED,ErrorCode.INVALID_TOKEN);
            }


            Consumer consumer = consumerRepository.findById(userId).orElseGet(() -> {
                User u = userRepository.findById(userId).orElseThrow();
                Consumer c = new Consumer();
                c.setUser(u);
                c.setConsumerTypes(new ArrayList<>());
                return consumerRepository.save(c);
            });

            List<Long> typeIds = (consumerRequest.getTypeIds() == null) ? new ArrayList<>() : new ArrayList<>(consumerRequest.getTypeIds());

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
            if (types.size() != typeIds.size()) {
                throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.INVALID_TYPE_ID);
            }

            consumer.getConsumerTypes().clear();
            consumer.getConsumerTypes().addAll(types);
            consumer.setDoSurvey(true);
            consumerRepository.save(consumer);

            var result = types.stream().map(t -> new ConsumerTypeResponse(t.getId(), t.getName())).toList();
            return ResponseEntity.ok(new ApiResponse(true, "Survey submitted",
                    new ConsumerResponse(consumer.getId(), result, consumer.isDoSurvey())));
    }
}
