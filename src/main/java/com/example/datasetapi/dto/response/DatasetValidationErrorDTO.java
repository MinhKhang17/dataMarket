package com.example.datasetapi.dto.response;

import com.example.datasetapi.enums.Datasets.ErrorCode;
import com.example.datasetapi.enums.Datasets.ValidationPhase;

import lombok.Data;

@Data
public class DatasetValidationErrorDTO {

    private ErrorCode errorCode;

    // Giai đoạn: SCHEMA_CHECK, MODERATION, ...
    private ValidationPhase validationPhase;

    // Cột nào bị lỗi
    private String columnName;

    // Dòng nào bị lỗi (0-based)
    private Long rowIndex;

    // Mô tả chi tiết lỗi
    private String message;


}

