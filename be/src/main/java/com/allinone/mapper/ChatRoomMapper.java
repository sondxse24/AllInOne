package com.allinone.mapper;

import com.allinone.dto.response.chat.ChatRoomResponse;
import com.allinone.entity.chat.ChatRoom;
import com.allinone.entity.iam.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatRoomMapper {

    // Chuyển từ ChatRoom Entity sang DTO
    // Chúng ta sẽ ignore field participants để xử lý thủ công bằng logic fetch User
    @Mapping(target = "participants", ignore = true)
    ChatRoomResponse toChatRoomResponse(ChatRoom chatRoom);

    // Chuyển từ Users Entity sang ParticipantInfo DTO
    @Mapping(target = "userId", expression = "java(user.getUserId().toString())")
    @Mapping(target = "userName", source = "username")
    ChatRoomResponse.ParticipantInfo toParticipantInfo(Users user);
}
