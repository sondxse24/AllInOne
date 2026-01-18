package com.allinone.repository.chat;

import com.allinone.entity.chat.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByUserId(String userId);

    @Query("SELECT p.chatRoom.id FROM ChatParticipant p " +
            "WHERE p.userId IN :userIds " +
            "AND p.chatRoom.isGroup = false " + // Đưa điều kiện lọc này lên WHERE
            "GROUP BY p.chatRoom.id " +
            "HAVING COUNT(DISTINCT p.userId) = :size")
    List<String> findExistingRoomForParticipants(@Param("userIds") List<String> userIds, @Param("size") long size);

    List<ChatParticipant> findByChatRoomId(String chatRoomId);
}