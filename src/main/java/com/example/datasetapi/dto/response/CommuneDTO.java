package com.example.datasetapi.dto.response;

import com.example.datasetapi.model.location.Province;
import lombok.Data;

import java.util.List;
@Data
public class CommuneDTO {
    private String CommuneID;
    private String CommuneName;
    private ProvinceDTO province;
}
