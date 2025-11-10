package com.example.datasetapi.service.survey;

import com.example.datasetapi.dto.request.ConsumerRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface SurveyService {
    ResponseEntity<ApiResponse> getOptionsForSurvey();
    ResponseEntity<ApiResponse> submitSurveyResponses(@RequestBody ConsumerRequest consumerRequest);
}
