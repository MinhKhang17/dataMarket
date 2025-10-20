package com.example.datasetapi.dto.response;

//import com.example.datasetapi.model.userManager.Address;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DatasetParentReposonseDto {

    private long datasetGroupId;

    private ProvinceDTO provinceDTO;

    private ProviderDto providerDTO;

    private int lasted_version;

    private LocalDateTime lasted_upload;

    private DatasetTypeDto datasetTypeDto;

    List<DatasetChildGroupDTO> datasetChildGroups;



}
