package com.allinone.controller;

import com.allinone.dto.base.ApiResponse;
import com.allinone.dto.request.chat.CreateRoomRequest;
import com.allinone.entity.iam.Users;
import com.allinone.entity.chat.ChatMessage;
import com.allinone.entity.chat.ChatParticipant;
import com.allinone.entity.chat.ChatRoom;
import com.allinone.repository.chat.ChatMessageRepository;
import com.allinone.repository.chat.ChatParticipantRepository;
import com.allinone.repository.chat.ChatRoomRepository;
import com.allinone.service.UsersService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatRestController {

    ChatRoomRepository chatRoomRepository;
    ChatParticipantRepository chatParticipantRepository;
    ChatMessageRepository chatMessageRepository;
    UsersService usersService;

    // 1. Lấy danh sách phòng chat của tôi
    @GetMapping("/my-rooms")
    public ApiResponse<List<ChatRoom>> getMyRooms() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        Users users = usersService.findByEmail(auth.getName());
        // Tìm các tham gia của user -> lấy ra list Room
        List<ChatRoom> rooms = chatParticipantRepository.findByUserId(users.getUserId().toString())
                .stream()
                .map(ChatParticipant::getChatRoom)
                .collect(Collectors.toList());

        return ApiResponse.<List<ChatRoom>>builder().result(rooms).build();
    }

    // 2. Tạo phòng chat mới (Nhóm hoặc 1-1)
    @PostMapping("/create")
    public ApiResponse<ChatRoom> createRoom(@RequestBody CreateRoomRequest request) {
        // LOG 1: Kiểm tra dữ liệu Frontend gửi lên
        System.out.println("🚀 Request Create Room: Name=" + request.getName() + ", Participants=" + request.getParticipantIds());

        if (request.getParticipantIds() == null || request.getParticipantIds().isEmpty()) {
            System.out.println("⚠️ Cảnh báo: Danh sách participantIds trống!");
        }

        boolean isGroup = request.getParticipantIds().size() > 2;

        ChatRoom room = ChatRoom.builder()
                .name(request.getName() != null ? request.getName() : "Chat")
                .isGroup(isGroup)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(room);
        System.out.println("✅ Đã lưu ChatRoom với ID: " + savedRoom.getId());

        for (String identifier : request.getParticipantIds()) {
            if (identifier == null || identifier.trim().isEmpty()) {
                System.out.println("⏩ Bỏ qua participant vì identifier bị null");
                continue;
            }

            try {
                Users member = usersService.findByEmail(identifier);
                if (member == null) {
                    System.err.println("❌ Không tìm thấy user với email: " + identifier);
                    continue;
                }

                ChatParticipant participant = ChatParticipant.builder()
                        .userId(member.getUserId().toString())
                        .chatRoom(savedRoom)
                        .build();

                chatParticipantRepository.save(participant);
            } catch (Exception e) {
                System.err.println("❌ Lỗi hệ thống khi thêm " + identifier);
            }
        }

        return ApiResponse.<ChatRoom>builder().result(savedRoom).build();
    }

    // 3. Lấy lịch sử tin nhắn của 1 phòng
    @GetMapping("/messages/{roomId}")
    public ApiResponse<List<ChatMessage>> getHistory(@PathVariable String roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(roomId);
        return ApiResponse.<List<ChatMessage>>builder().result(messages).build();
    }
}