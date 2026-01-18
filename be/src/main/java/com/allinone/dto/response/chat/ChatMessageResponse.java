package com.allinone.dto.response.chat;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    String id;
    String content;
    String senderId;
    String senderName;
    String senderAvatar;
    LocalDateTime timestamp;
}