package com.example.datasetapi.dto.response;

import com.example.datasetapi.enums.Datasets.PricingMethod;
import lombok.Data;

import java.util.List;
@Data
public class DatasetPLanWithPricingDTO {
    private PricingMethod pricingMethod;
    List<DatasetPricingDTO> datasetPricingDTOList;
}
