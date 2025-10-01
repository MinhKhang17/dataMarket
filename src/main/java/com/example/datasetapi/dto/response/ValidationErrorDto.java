package com.example.datasetapi.dto.response;

import com.example.datasetapi.enums.Datasets.ErrorCode;
import com.example.datasetapi.enums.Datasets.ValidationPhase;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationErrorDto {
    private ValidationPhase phase;
    private ErrorCode code;
    private String columnName;
    private Long rowIndex;
    private String message;
}