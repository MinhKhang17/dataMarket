package com.example.datasetapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
        ErrorCode error = ex.getErrorCode();

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", error.getCode());
        response.put("message", error.getMessage());

        // ✅ Trả về kèm HttpStatus của exception
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(response);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "INTERNAL_SERVER_ERROR");
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(500)
                .body(response);
    }
}
