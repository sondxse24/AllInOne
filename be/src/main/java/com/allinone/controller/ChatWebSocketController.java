package com.allinone.controller;

import com.allinone.dto.request.chat.ChatMessageRequest;
import com.allinone.dto.response.chat.ChatMessageResponse;
import com.allinone.service.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWebSocketController {

    SimpMessagingTemplate messagingTemplate;
    ChatService chatService;

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessageRequest request) {

        // 1. Gọi Service để xử lý logic lưu DB và lấy thông tin User
        ChatMessageResponse response = chatService.saveMessage(roomId, request);

        // 2. Bắn DTO hoàn chỉnh ra cho các client trong phòng
        messagingTemplate.convertAndSend("/topic/room/" + roomId, response);
    }
}