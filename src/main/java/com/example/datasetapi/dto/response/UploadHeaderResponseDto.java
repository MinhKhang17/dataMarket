package com.example.datasetapi.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class UploadHeaderResponseDto {
  private long datasetInformationId;
  //set neu nhu loi header
  private List<ValidationErrorDto> validationErrorDtos;
}
