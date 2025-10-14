package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;

import java.util.List;

public interface LocationService {
    public List<Province> getAllProvinces();
    public List<Commune> getAllCommunesByProvinceId(String provinceId);
}
