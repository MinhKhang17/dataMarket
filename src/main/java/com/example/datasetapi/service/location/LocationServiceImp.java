package com.example.datasetapi.service.location;

import com.example.datasetapi.dto.response.CommuneDTO;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import com.example.datasetapi.repository.CommuneRepository;
import com.example.datasetapi.repository.ProvinceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImp implements LocationService {

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private CommuneRepository communeRepository;

    @Override
    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }

    @Override
    public List<CommuneDTO> getCommunesByProvinceId(String provinceId) {
        List<Commune> communes = communeRepository.findByProvince_IdProvince(provinceId);
        return communes.stream()
                .map(c -> new CommuneDTO(
                        c.getIdCommune(),
                        c.getName(),
                        c.getProvince().getIdProvince(),
                        c.getProvince().getName()
                ))
                .toList();
    }

}
