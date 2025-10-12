package com.example.datasetapi.exception;

import lombok.Data;

public enum ErrorCode {
    //lỗi người dùng
    USER_NOT_FOUND("USER_NOT_FOUND", "Tài khoản không tồn tại"),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", "Tên tài khoản đã được sử dụng"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email đã được đăng ký"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Tên đăng nhập hoặc mật khẩu không đúng"),
    ACCOUNT_DISABLED("ACCOUNT_DISABLED", "Tài khoản đã bị vô hiệu hóa"),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", "Tài khoản đã bị khóa"),
    PASSWORD_TOO_WEAK("PASSWORD_TOO_WEAK", "Mật khẩu quá yếu"),
    PASSWORD_TOO_SHORT("PASSWORD_TOO_SHORT","Mật khẩu quá ngắn" ),
    INVALID_TOKEN("INVALID_TOKEN", "Token không hợp lệ hoặc đã hết hạn"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token đã hết hạn"),
    UNAUTHORIZED("UNAUTHORIZED", "Bạn không có quyền truy cập"),
    REFRESH_TOKEN_INVALID("REFRESH_TOKEN_INVALID", "Refresh token không hợp lệ"),
    LOGOUT_FAILED("LOGOUT_FAILED", "Đăng xuất thất bại"),
    EMAIL_INVALID("EMAIL_INVALID","Email không hợp lệ"),
    //lỗi dataset
    DATASET_NOT_FOUND("DATASET_NOT_FOUND", "Dataset không tồn tại"),
    DATASET_ALREADY_EXISTS("DATASET_ALREADY_EXISTS", "Dataset đã tồn tại"),
    DATASET_GROUP_CONFLICT("DATASET_GROUP_CONFLICT", "Dataset đã được gán vào group này"),


    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Lỗi hệ thống, vui lòng thử lại sau"),
    BAD_REQUEST("BAD_REQUEST", "Yêu cầu không hợp lệ"),
    INVALID_ROLE("INVALID_ROLE","role không hợp lệ" ),
    LOCAL_DATE_INVALID("LOCAL_DATE_INVALID","Ngày giờ không hợp lệ format chuẩn: yyyy-MM-dd"),
    DATASET_ROW_MIN_INVALID("DATASET_ROW_MIN_INVALID","Gói quá nhỏ" ),
    DATASET_PACK_INVALID("DATASET_PACK_INVALID","Gói dataset chưa được phân loại package" ),
    PRICING_RULE_INVALID("PRICING_RULE_INVALID","Không tìm thấy pricing rule" );

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
