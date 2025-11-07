package com.example.datasetapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponse {
    private Long id;
    private String name;
    private String email;
    private String organizationName;
    private String phoneNumber;
    private String taxId;
    private String location;
    private List<String> imageUrl;

}
