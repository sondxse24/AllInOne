package com.allinone.controller;

import com.allinone.dto.base.ApiResponse;
import com.allinone.dto.request.users.CreateUsersRequest;
import com.allinone.dto.response.users.UsersResponse;
import com.allinone.service.UsersService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UsersController {

    UsersService usersService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createUser(@RequestBody CreateUsersRequest user) {
        UsersResponse users = usersService.createUser(user);
        return ResponseEntity.ok(
                ApiResponse.<UsersResponse>builder()
                        .code(200)
                        .message("Create user successfully")
                        .result(users)
                        .build()
        );
    }

    @GetMapping("/no")
    public ResponseEntity<ApiResponse<?>> noUser(@RequestParam int no) {
        UsersResponse users = usersService.findUserByNumericalOrder(no);
        return ResponseEntity.ok(
                ApiResponse.<UsersResponse>builder()
                        .code(200)
                        .message("Get user by no successfully")
                        .result(users)
                        .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser() {
        return ResponseEntity.ok(
                ApiResponse.<UsersResponse>builder()
                        .code(200)
                        .message("Get my info successfully")
                        .result(usersService.getMe())
                        .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.<List<UsersResponse>>builder()
                        .code(200)
                        .message("Get my info successfully")
                        .result(usersService.getAllUsers())
                        .build()
        );
    }

    @GetMapping("/username")
    public ResponseEntity<ApiResponse<?>> getUserByUsername(@RequestParam String username) {
        return ResponseEntity.ok(
                ApiResponse.<List<UsersResponse>>builder()
                        .code(200)
                        .message("Get my info successfully")
                        .result(usersService.getAllUsersByName(username))
                        .build()
        );
    }
}
