package com.allinone.entity.chat;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_room")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name; // Tên nhóm (Ví dụ: "Hội anh em", hoặc null nếu là chat 1-1)

    private boolean isGroup; // true: Nhóm, false: Chat 1-1

    // Quan hệ 1-N với tin nhắn
    @OneToMany(mappedBy = "chatRoom")
    private List<ChatMessage> messages;
}