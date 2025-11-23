package com.example.datasetapi.controller.download;

import com.example.datasetapi.service.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
public class APIController {
    @Autowired
    private DatasetService datasetService;
    @PostMapping("dataset/api/data")
    public ResponseEntity<?> getDataFromApi(@RequestParam String token){
        return  datasetService.getDataForApiBuying(token);
    }
}
