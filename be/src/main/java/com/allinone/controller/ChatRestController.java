package com.allinone.controller;

import com.allinone.dto.base.ApiResponse;
import com.allinone.dto.request.chat.CreateRoomRequest;
import com.allinone.dto.response.chat.ChatMessageResponse;
import com.allinone.dto.response.chat.ChatRoomResponse;
import com.allinone.repository.chat.ChatMessageRepository;
import com.allinone.service.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatRestController {
    ChatService chatService;

    @GetMapping("/my-rooms")
    public ApiResponse<List<ChatRoomResponse>> getMyRooms() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        return ApiResponse.<List<ChatRoomResponse>>builder()
                .result(chatService.getMyRooms(auth.getName()))
                .build();
    }

    @PostMapping("/create")
    public ApiResponse<ChatRoomResponse> createRoom(@RequestBody CreateRoomRequest request) {
        return ApiResponse.<ChatRoomResponse>builder()
                .result(chatService.createRoom(request))
                .build();
    }

    @GetMapping("/messages/{roomId}")
    public ApiResponse<List<ChatMessageResponse>> getMessages(@PathVariable String roomId) {
        return ApiResponse.<List<ChatMessageResponse>>builder()
                .result(chatService.getMessages(roomId))
                .build();
    }
}