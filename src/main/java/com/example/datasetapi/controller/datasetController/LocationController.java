package com.example.datasetapi.controller.datasetController;

import com.example.datasetapi.dto.response.CommuneDTO;
import com.example.datasetapi.model.location.Province;
import com.example.datasetapi.service.location.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/location/")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping("/getAll")
    public ResponseEntity<List<Province>> getAllProvinces() {
        List<Province> provinces = locationService.getAllProvinces();
        return ResponseEntity.ok(provinces);
    }


    @GetMapping("/getAll/{id}")
    public ResponseEntity<List<CommuneDTO>> getCommunesByProvinceId(
            @PathVariable("id") String provinceId) {
        return ResponseEntity.ok(locationService.getCommunesByProvinceId(provinceId));
    }

}
