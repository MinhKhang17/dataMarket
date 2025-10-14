package com.example.datasetapi.service.Location;

import com.example.datasetapi.dto.response.CommuneDTO;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;

import java.util.List;

public interface LocationService {
    List<Province> getAllProvinces();
    List<CommuneDTO> getCommunesByProvinceId(String provinceId);
}
