package com.allinone.serviceImpl;

import com.allinone.dto.request.chat.ChatMessageRequest;
import com.allinone.dto.request.chat.CreateRoomRequest;
import com.allinone.dto.response.chat.ChatMessageResponse;
import com.allinone.dto.response.chat.ChatRoomResponse;
import com.allinone.entity.chat.ChatMessage;
import com.allinone.entity.chat.ChatParticipant;
import com.allinone.entity.chat.ChatRoom;
import com.allinone.entity.iam.Users;
import com.allinone.exception.AppException;
import com.allinone.exception.ErrorCode;
import com.allinone.mapper.ChatRoomMapper;
import com.allinone.repository.chat.ChatMessageRepository;
import com.allinone.repository.chat.ChatParticipantRepository;
import com.allinone.repository.chat.ChatRoomRepository;
import com.allinone.service.ChatService;
import com.allinone.service.UsersService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatServiceImpl implements ChatService {
    ChatRoomRepository chatRoomRepository;
    ChatParticipantRepository chatParticipantRepository;
    ChatMessageRepository chatMessageRepository;
    UsersService usersService;
    ChatRoomMapper chatRoomMapper; // Inject Mapper

    @Override
    public ChatRoomResponse createRoom(CreateRoomRequest request) {
        List<String> participantIdentifiers = request.getParticipantIds();

        List<Users> members = participantIdentifiers.stream()
                .map(usersService::findByEmail)
                .filter(java.util.Objects::nonNull)
                .toList();

        List<String> memberIds = members.stream()
                .map(m -> m.getUserId().toString())
                .toList();

        // Check trùng cho 1-1
        if (memberIds.size() == 2) {
            List<String> existingRooms = chatParticipantRepository.findExistingRoomForParticipants(memberIds, memberIds.size());
            if (!existingRooms.isEmpty()) {
                return chatRoomRepository.findById(existingRooms.getFirst())
                        .map(this::enrichChatRoomResponse) // Dùng hàm bổ trợ thông tin user
                        .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
            }
        }

        boolean isGroup = memberIds.size() > 2;
        ChatRoom room = ChatRoom.builder()
                .name(request.getName() != null && !request.getName().isEmpty() ? request.getName() : (isGroup ? "Nhóm mới" : null))
                .isGroup(isGroup)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(room);

        List<ChatParticipant> participants = memberIds.stream()
                .map(id -> ChatParticipant.builder().userId(id).chatRoom(savedRoom).build())
                .toList();
        chatParticipantRepository.saveAll(participants);

        return enrichChatRoomResponse(savedRoom);
    }

    @Override
    public List<ChatRoomResponse> getMyRooms(String email) {
        Users currentUser = usersService.findByEmail(email);

        return chatParticipantRepository.findByUserId(currentUser.getUserId().toString())
                .stream()
                .map(p -> enrichChatRoomResponse(p.getChatRoom()))
                .toList();
    }

    @Override
    public List<ChatMessageResponse> getMessages(String roomId) {
        // 1. Kiểm tra phòng tồn tại (Optional)
        if (!chatRoomRepository.existsById(roomId)) {
            throw new RuntimeException("Room not found");
        }

        // 2. Lấy list entity từ DB (giả sử bạn có hàm tìm theo RoomId và sắp xếp thời gian)
        // Bạn cần đảm bảo trong ChatMessageRepository có method: List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(String roomId);
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(roomId);

        // 3. Map sang DTO
        return messages.stream().map(msg -> {
            // Lấy thông tin người gửi để hiển thị Avatar/Tên bên frontend
            // Lưu ý: userId lưu trong msg có thể là String, cần convert nếu cần
            Users sender = null;
            try {
                // Giả sử senderId trong ChatMessage là username hoặc userId
                // Nếu là userId (UUID):
                sender = usersService.findByUserId(UUID.fromString(msg.getSenderId()));
                // Nếu là username:
                // sender = usersService.findByUsername(msg.getSenderId());
            } catch (Exception e) {
                // Log error or ignore if user deleted
            }

            return ChatMessageResponse.builder()
                    .id(String.valueOf(msg.getId()))
                    .content(msg.getContent())
                    .senderId(msg.getSenderId())
                    .senderName(sender != null ? sender.getUsername() : "Unknown") // Fallback nếu ko tìm thấy user
                    .senderAvatar(sender != null ? sender.getAvatar() : null)
                    .timestamp(msg.getTimestamp())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public ChatMessageResponse saveMessage(String roomId, ChatMessageRequest request) {
        // 1. Kiểm tra phòng
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // 2. Tạo Entity và Lưu
        ChatMessage message = ChatMessage.builder()
                .content(request.getContent())
                .senderId(request.getSenderId())
                .timestamp(LocalDateTime.now())
                .chatRoom(room)
                .build();

        ChatMessage savedMsg = chatMessageRepository.save(message);

        // 3. Lấy thông tin User để map vào DTO (Fix lỗi Unknown)
        String senderName = "Unknown";
        String senderAvatar = null;

        try {
            Users sender = usersService.findByUserId(UUID.fromString(savedMsg.getSenderId()));
            if (sender != null) {
                senderName = sender.getUsername(); // Hoặc getFullName()
                senderAvatar = sender.getAvatar();
            }
        } catch (Exception e) {
            // Log lỗi nếu không tìm thấy user (để tránh crash luồng socket)
            System.out.println("Cannot find user for message: " + e.getMessage());
        }

        // 4. Trả về DTO
        return ChatMessageResponse.builder()
                .id(String.valueOf(savedMsg.getId()))
                .content(savedMsg.getContent())
                .senderId(savedMsg.getSenderId())
                .senderName(senderName)
                .senderAvatar(senderAvatar)
                .timestamp(savedMsg.getTimestamp())
                .build();
    }

    // Hàm bổ trợ để gộp thông tin User vào DTO sau khi Mapper đã chạy xong
    private ChatRoomResponse enrichChatRoomResponse(ChatRoom room) {
        ChatRoomResponse response = chatRoomMapper.toChatRoomResponse(room);

        List<ChatRoomResponse.ParticipantInfo> participantInfos = chatParticipantRepository.findByChatRoomId(room.getId())
                .stream()
                .map(p -> {
                    Users u = usersService.findByUserId(UUID.fromString(p.getUserId()));
                    return chatRoomMapper.toParticipantInfo(u);
                })
                .toList();

        response.setParticipants(participantInfos);
        return response;
    }
}