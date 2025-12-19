package com.allinone.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {

    //AUTHENTICATION ERRORS
    LOGIN_FAILED(1000, "Email or Password is invalid!", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "You do not have permission", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_NOT_FOUND(1003, "Refresh token not found", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REVOKED(1004, "Refresh token has been revoked", HttpStatus.FORBIDDEN),
    INVALID_TOKEN_TYPE(1005, "Invalid token type", HttpStatus.BAD_REQUEST),
    LOGOUT_FAILED(1006, "Logout failed", HttpStatus.INTERNAL_SERVER_ERROR),

    //USER ERRORS (2000 - 2999)
    USER_EXISTED(2000, "Email already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(2001, "User not found", HttpStatus.NOT_FOUND),
    USER_NOT_AUTHENTICATED(2003, "User not authenticated context", HttpStatus.UNAUTHORIZED),

    //SYSTEM ERRORS (9999)
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR);

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}