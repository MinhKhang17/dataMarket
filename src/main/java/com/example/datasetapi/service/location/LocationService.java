package com.example.datasetapi.service.location;

import com.example.datasetapi.dto.response.CommuneDTO;
import com.example.datasetapi.model.location.Province;

import java.util.List;

public interface LocationService {
    List<Province> getAllProvinces();
    List<CommuneDTO> getCommunesByProvinceId(String provinceId);
}
