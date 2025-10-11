package com.example.datasetapi.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public Map<String, Object> handleCustomException(CustomException ex) {
        ErrorCode error = ex.getErrorCode();
        return Map.of(
                "success", false,
                "code", error.getCode(),
                "message", error.getMessage()
        );
    }
}
