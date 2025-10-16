package com.example.datasetapi.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    public CustomException(HttpStatus httpStatus, ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
    public CustomException(HttpStatus status, String message) {
        super(message);
        this.httpStatus = status;
        this.errorCode = null; // hoặc ErrorCode.DEFAULT
    }
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }


}
