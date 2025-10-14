package com.example.datasetapi.controller.datasetController;

import com.example.datasetapi.dto.response.CommuneDTO;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import com.example.datasetapi.service.Location.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/location/")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping("province")
    public ResponseEntity<List<Province>> getAllProvinces() {
        List<Province> provinces = locationService.getAllProvinces();
        return ResponseEntity.ok(provinces);
    }


    @GetMapping("communes")
    public ResponseEntity<List<CommuneDTO>> getCommunesByProvinceId(
            @RequestParam("provinceId") String provinceId) {
        return ResponseEntity.ok(locationService.getCommunesByProvinceId(provinceId));
    }
}
