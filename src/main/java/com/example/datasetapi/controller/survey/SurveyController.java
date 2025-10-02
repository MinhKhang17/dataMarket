package com.example.datasetapi.controller.survey;

import com.example.datasetapi.dto.request.ConsumerRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.survey.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {
    @Autowired
    private SurveyService surveyService;

    // Get all consumer types
    @GetMapping("/options")
    public ResponseEntity<ApiResponse> getOption() {
        return surveyService.getOptionsForSurvey();
    }

    @PutMapping("/submit")
    public ResponseEntity<ApiResponse> submitSurvey(@RequestBody ConsumerRequest request) {
        return surveyService.submitSurveyResponses(request);
    }
}
