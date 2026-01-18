package com.allinone.listener;

import com.allinone.dto.response.users.UserStatusResponse;
import com.allinone.entity.iam.Users;
import com.allinone.repository.iam.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UsersRepository usersRepository;

    // 1. Khi User kết nối Socket -> Set Online -> Bắn thông báo
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        // Lấy username từ Principal (Config Security của bạn phải chuẩn)
        String username = Objects.requireNonNull(headerAccessor.getUser()).getName();

        if (username != null) {
            updateUserStatus(username, true);
        }
    }

    // 2. Khi User mất kết nối (tắt tab, rớt mạng) -> Set Offline -> Bắn thông báo
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        // Lưu ý: Lúc disconnect có thể principal vẫn còn lấy được
        if (headerAccessor.getUser() != null) {
            String username = headerAccessor.getUser().getName();
            if (username != null) {
                log.info("User Disconnected: {}", username);
                updateUserStatus(username, false);
            }
        }
    }

    private void updateUserStatus(String identity, boolean isOnline) {
        // Log xem cái identity đang là Email hay Username
        log.info("\uD83D\uDD0D Đang tìm User trong DB với key: {}", identity);

        Users user = usersRepository.findByEmail(identity).orElse(null);

        // Update DB
        assert user != null;
        user.setOnline(isOnline);
        usersRepository.save(user);
        log.info("✅ Đã update DB isOnline={} cho user: {}", isOnline, user.getUsername());

        // Bắn Socket
        UserStatusResponse statusDto = new UserStatusResponse(user.getUserId().toString(), isOnline);
        messagingTemplate.convertAndSend("/topic/public.status", statusDto);
        log.info("🚀 Đã bắn tin nhắn tới /topic/public.status");
    }
}
