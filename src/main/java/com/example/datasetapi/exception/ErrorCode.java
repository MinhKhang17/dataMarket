package com.example.datasetapi.exception;

public enum ErrorCode {

    //Tokken errors
    INVALID_TOKEN("INVALID_TOKEN", "Token is invalid or expired"),
    UNAUTHORIZED("UNAUTHORIZED", "You are not authorized to access this resource"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token has expired"),
    REFRESH_TOKEN_INVALID("REFRESH_TOKEN_INVALID", "Refresh token is invalid"),
    TOKEN_NOT_FOUND("TOKEN_NOT_FOUND", "Token not found"),

    //User errors
    USER_NOT_FOUND("USER_NOT_FOUND", "User account not found"),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", "Username is already taken"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email is already registered"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid username or password"),
    ACCOUNT_DISABLED("ACCOUNT_DISABLED", "User account is disabled"),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", "User account is locked"),
    LOGOUT_FAILED("LOGOUT_FAILED", "Logout failed"),
    EMAIL_INVALID("EMAIL_INVALID", "Invalid email format"),

    PASSWORD_TOO_WEAK("PASSWORD_TOO_WEAK", "Password is too weak"),
    PASSWORD_TOO_SHORT("PASSWORD_TOO_SHORT", "Password is too short"),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", "Passwords do not match"),
    OLD_PASSWORD_INCORRECT("OLD_PASSWORD_INCORRECT", "Old password is incorrect"),

    // Input errors
    REGISTRATION_DISABLED("REGISTRATION_DISABLED", "User registration is currently disabled"),
    INVALID_INPUT("INVALID_INPUT", "Invalid input data"),
    MISSING_REQUIRED_FIELD("MISSING_REQUIRED_FIELD", "Missing required field"),

    // Dataset errors
    DATASET_NOT_FOUND("DATASET_NOT_FOUND", "Dataset not found"),
    DATASET_ALREADY_EXISTS("DATASET_ALREADY_EXISTS", "Dataset already exists"),
    DATASET_GROUP_CONFLICT("DATASET_GROUP_CONFLICT", "Dataset is already assigned to this group"),
    DATASET_NOT_PENDING("DATASET_NOT_PENDING", "Dataset is not in pending status"),
    DATASET_INFO_NOT_APPROVED("DATASET_INFO_NOT_APPROVED", "Dataset information is not approved yet"),
    //
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal server error. Please try again later"),
    BAD_REQUEST("BAD_REQUEST", "Invalid request"),
    INVALID_ROLE("INVALID_ROLE", "Invalid user role"),
    LOCAL_DATE_INVALID("LOCAL_DATE_INVALID", "Invalid date format. Expected format: yyyy-MM-dd"),

    // Wallet errors
    WALLET_NOT_FOUND("WALLET_NOT_FOUND", "Wallet not found"),
    INVALID_WITHDRAW_AMOUNT("INVALID_WITHDRAW_AMOUNT", "Withdraw amount must be greater than 0"),
    INSUFFICIENT_FUNDS("INSUFFICIENT_FUNDS", "Not enough balance in wallet"),

    // Survey errors
    INVALID_TYPE_ID("INVALID_TYPE_ID", "One or more provided type IDs are invalid"),


  DATASET_ROW_MIN_INVALID("DATASET_ROW_MIN_INVALID","Gói quá nhỏ" ),
    DATASET_PACK_INVALID("DATASET_PACK_INVALID","Gói dataset chưa được phân loại package" ),
    PRICING_RULE_INVALID("PRICING_RULE_INVALID","Không tìm thấy pricing rule" ),
    Location_NOT_FOUND("Location_NOT_FOUND","Không tìm thấy location"),
    COMMUNE_NOT_FOUND("COMMUNE_NOT_FOUND","Không tìm thấy commmune" );


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
