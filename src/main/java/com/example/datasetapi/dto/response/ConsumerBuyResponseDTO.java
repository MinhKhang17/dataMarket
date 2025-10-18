package com.example.datasetapi.dto.response;

import com.example.datasetapi.dto.service.BuyApiInforDTO;
import com.example.datasetapi.dto.service.BuyOnTimeInfoDTO;
import com.example.datasetapi.dto.service.BuySubInfoDTO;
import lombok.Data;

@Data
public class ConsumerBuyResponseDTO {
        private BuyOnTimeInfoDTO buyOnTimeInfoDTO;
        private BuySubInfoDTO buySubInfoDTO;
        private BuyApiInforDTO buyApiInforDTO;
}
