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
            log.info("User Connected: " + username);
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
                log.info("User Disconnected: " + username);
                updateUserStatus(username, false);
            }
        }
    }

    // Hàm cập nhật DB và bắn socket ra toàn server
    private void updateUserStatus(String username, boolean isOnline) {
        var users = usersRepository.findByUsername(username); // Hoặc findByEmail tùy auth của bạn
        if (!users.isEmpty()) {
            Users user = users.getFirst();

            user.setOnline(isOnline);
            usersRepository.save(user);

            UserStatusResponse statusDto = new UserStatusResponse(user.getUserId().toString(), isOnline);
            messagingTemplate.convertAndSend("/topic/public.status", statusDto);
        }
    }
}
