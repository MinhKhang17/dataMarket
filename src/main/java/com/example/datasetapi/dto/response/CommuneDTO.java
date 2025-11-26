package com.example.datasetapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommuneDTO {
    private String idCommune;
    private String name;
    private String provinceId;
    private String provinceName;

}