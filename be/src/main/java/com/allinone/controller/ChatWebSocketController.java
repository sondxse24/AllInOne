package com.allinone.controller;

import com.allinone.dto.request.chat.ChatMessageRequest;
import com.allinone.entity.chat.ChatMessage;
import com.allinone.entity.chat.ChatRoom;
import com.allinone.repository.chat.ChatMessageRepository;
import com.allinone.repository.chat.ChatRoomRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWebSocketController {

    SimpMessagingTemplate messagingTemplate;
    ChatMessageRepository chatMessageRepository;
    ChatRoomRepository chatRoomRepository;

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessageRequest request) {

        // 1. Kiểm tra phòng tồn tại
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // 2. Tạo Entity tin nhắn
        ChatMessage message = ChatMessage.builder()
                .content(request.getContent())
                .senderId(request.getSenderId()) // Lấy từ payload hoặc từ Principal (nếu config Auth cho socket)
                .timestamp(LocalDateTime.now())
                .chatRoom(room)
                .build();

        // 3. Lưu vào Database (QUAN TRỌNG: Để F5 không mất)
        ChatMessage savedMsg = chatMessageRepository.save(message);

        // 4. Bắn tin nhắn ra cho mọi người trong phòng
        // Đường dẫn trả về client: /topic/room/{roomId}
        messagingTemplate.convertAndSend("/topic/room/" + roomId, savedMsg);
    }
}