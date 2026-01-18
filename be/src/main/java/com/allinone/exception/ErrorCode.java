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

    // FRIENDSHIP ERRORS (3000 - 3999)
    FRIEND_REQUEST_NOT_FOUND(3000, "Friend request not found", HttpStatus.NOT_FOUND),
    ALREADY_FRIENDS(3001, "You are already friends with this user", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_PENDING(3002, "A friend request is already pending", HttpStatus.BAD_REQUEST),
    CANNOT_ADD_SELF(3003, "You cannot add yourself as a friend", HttpStatus.BAD_REQUEST),
    INVALID_FRIEND_STATUS(3004, "Invalid friend status provided", HttpStatus.BAD_REQUEST),

    // CHAT ERRORS (4000 - 4999)
    ROOM_ALREADY_EXISTS(4000, "Phòng chat giữa hai người đã tồn tại", HttpStatus.CONFLICT),

    //SYSTEM ERRORS (9999)
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR);

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}