package com.allinone.controller;

import com.allinone.dto.base.ApiResponse;
import com.allinone.dto.request.friend.AddFriendRequest;
import com.allinone.dto.request.friend.DeleteFriendRequest;
import com.allinone.dto.request.friend.ResponseAddFriendRequest;
import com.allinone.dto.response.friend.FriendListResponse;
import com.allinone.dto.response.friend.FriendResponse;
import com.allinone.service.FriendService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FriendController {

    FriendService friendService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('MEMBER')")
    public ResponseEntity<ApiResponse<?>> addFriend(
            @RequestBody AddFriendRequest createFriendRequest) {
        friendService.addFriend(createFriendRequest);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(200)
                        .message("Add friend successfully")
                        .result(null)
                        .build()
        );
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFriendRequests() {
        return ResponseEntity.ok(
                ApiResponse.<List<FriendResponse>>builder()
                        .code(200)
                        .result(friendService.getAddFriendList())
                        .build()
        );
    }

    @PutMapping("/accept")
    public ResponseEntity<ApiResponse<?>> acceptFriend(
            @RequestBody ResponseAddFriendRequest acceptFriendRequest) {
        friendService.acceptFriend(acceptFriendRequest);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(200)
                        .message("Accept friend successfully")
                        .result(null)
                        .build()
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<?>> deleteFriend(
            @RequestBody DeleteFriendRequest deleteFriendRequest) {
        friendService.deleteFriend(deleteFriendRequest);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(200)
                        .message("Delete friend successfully")
                        .result(null)
                        .build()
        );
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<?>> getFriends() {
        return ResponseEntity.ok(
                ApiResponse.<List<FriendListResponse>>builder()
                        .code(200)
                        .message("Delete friend successfully")
                        .result(friendService.getListFriend())
                        .build()
        );
    }
}
