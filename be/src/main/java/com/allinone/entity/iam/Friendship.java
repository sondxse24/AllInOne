package com.allinone.entity.iam;

import com.allinone.constrant.FriendStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String requesterId; // ID người gửi lời mời
    private String addresseeId; // ID người nhận lời mời

    @Enumerated(EnumType.STRING)
    private FriendStatus status; // PENDING, ACCEPTED, DECLINED

    private LocalDateTime createdAt;
}
