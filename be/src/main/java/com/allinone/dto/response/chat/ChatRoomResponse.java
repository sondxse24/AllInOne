package com.allinone.dto.response.chat;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    String id;
    String name;
    boolean isGroup;
    List<ParticipantInfo> participants;

    @Data
    @Builder
    public static class ParticipantInfo {
        String userId;
        String userName;
        String email;
        String avatar;
    }
}