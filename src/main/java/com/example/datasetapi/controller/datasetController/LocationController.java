package com.example.datasetapi.controller.datasetController;

import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/location/")
public class LocationController {
    @GetMapping
    public ResponseEntity<?> getAllProvinces() {return null;}
    @GetMapping
    public ResponseEntity<?> getAllCommunesByProvineId() {return null;}
}
