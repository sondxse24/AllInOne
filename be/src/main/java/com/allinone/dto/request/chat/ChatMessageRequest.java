package com.allinone.dto.request.chat;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private String content;
    private String senderId;
}