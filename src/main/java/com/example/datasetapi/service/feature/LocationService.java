package com.example.datasetapi.service.feature;

import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LocationService {
    public List<Province> getAllProvinces();
    public List<Commune> getAllCommunesByProvinceId(String provinceId);

}
