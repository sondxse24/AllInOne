package com.allinone.controller;

import com.allinone.dto.base.ApiResponse;
import com.allinone.dto.request.users.CreateUsersRequest;
import com.allinone.dto.response.users.UsersResponse;
import com.allinone.entity.Users;
import com.allinone.service.UsersService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UsersController {

    UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UsersResponse>> createUser(@RequestBody CreateUsersRequest user) {
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
    public ResponseEntity<ApiResponse<UsersResponse>> noUser(@RequestParam int no) {
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
    public ResponseEntity<ApiResponse<UsersResponse>> getCurrentUser() {
        return ResponseEntity.ok(
                ApiResponse.<UsersResponse>builder()
                        .code(200)
                        .message("Get my info successfully")
                        .result(usersService.getMe())
                        .build()
        );
    }
}
