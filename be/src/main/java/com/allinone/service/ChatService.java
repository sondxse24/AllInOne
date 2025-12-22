package com.allinone.service;

import com.allinone.dto.request.chat.ChatMessageRequest;
import com.allinone.dto.request.chat.CreateRoomRequest;
import com.allinone.dto.response.chat.ChatMessageResponse;
import com.allinone.dto.response.chat.ChatRoomResponse;

import java.util.List;

public interface ChatService {
    ChatRoomResponse createRoom(CreateRoomRequest request);

    List<ChatRoomResponse> getMyRooms(String email);

    List<ChatMessageResponse> getMessages(String roomId);

    ChatMessageResponse saveMessage(String roomId, ChatMessageRequest request);
}
