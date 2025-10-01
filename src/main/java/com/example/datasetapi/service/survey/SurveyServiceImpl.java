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
import com.example.datasetapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SurveyServiceImpl implements SurveyService {
    @Autowired
    private ConsumerRepository consumerRepository;
    @Autowired
    private ConsumerTypeRepository consumerTypeRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private jakarta.servlet.http.HttpServletRequest request;
    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseEntity<ApiResponse> getOptionsForSurvey() {
        try {
            var options = consumerTypeRepository.findAll().stream()
                    .map(t -> new TypeOptionResponse(t.getId(), t.getName(), false))
                    .toList();
            return ResponseEntity.ok(new ApiResponse(true, "OK", options));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse> submitSurveyResponses(ConsumerRequest consumerRequest) {
        try {
            String auth = request.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer "))
                return ResponseEntity.status(401).body(new ApiResponse(false,"Missing/invalid Authorization",null));
            Long userId = jwtUtil.getUserIdFromToken(auth.substring(7));
            if (userId == null)
                return ResponseEntity.status(401).body(new ApiResponse(false,"Invalid or expired token",null));

            // Lấy consumer nếu có, nếu chưa có thì tạo mới
            Consumer consumer = consumerRepository.findById(userId).orElseGet(() -> {
                User u = userRepository.findById(userId).orElseThrow();
                Consumer c = new Consumer();
                c.setUser(u);
                c.setConsumerTypes(new ArrayList<>());
                return consumerRepository.save(c);
            });

            List<Long> typeIds = (consumerRequest.getTypeIds() == null) ? List.of() : consumerRequest.getTypeIds();
            List<ConsumerType> types = typeIds.isEmpty() ? List.of() : consumerTypeRepository.findAllById(typeIds);
            if (types.size() != typeIds.size())
                return ResponseEntity.badRequest().body(new ApiResponse(false,"Some typeIds are invalid",null));

            consumer.getConsumerTypes().clear();
            consumer.getConsumerTypes().addAll(types);
            consumerRepository.save(consumer);

            var result = types.stream().map(t -> new ConsumerTypeResponse(t.getId(), t.getName())).toList();
            return ResponseEntity.ok(new ApiResponse(true,"Survey submitted",
                    new ConsumerResponse(consumer.getId(), result)));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
