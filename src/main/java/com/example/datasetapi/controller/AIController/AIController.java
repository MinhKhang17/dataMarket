package com.example.datasetapi.controller.AIController;

import com.example.datasetapi.dto.response.AnalyticsSummaryDto;
import com.example.datasetapi.service.ai.AiAnalyticsMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController()
@RequestMapping("api/")
public class AIController {

 @Autowired
 private  AiAnalyticsMapperService service;

     @PostMapping("/dataset/ai/analysis")
     public AnalyticsSummaryDto datasetAnalysis(@RequestBody   long datasetId){
         try {
             return service.datasetAnalistByAi(datasetId);
         } catch (IllegalArgumentException ex) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
         }
     }


}
