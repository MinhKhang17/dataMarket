package com.example.datasetapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommuneDTO {
    private String idCommune;
    private String name;
    private String provinceId;
    private String provinceName;

    public CommuneDTO() {

    }
}