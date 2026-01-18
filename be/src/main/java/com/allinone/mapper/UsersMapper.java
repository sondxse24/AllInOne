package com.allinone.mapper;

import com.allinone.dto.response.friend.FriendListResponse;
import com.allinone.dto.response.users.UsersResponse;
import com.allinone.entity.iam.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsersMapper {

    @Mapping(target = "role", expression = "java(users.getRole().name())")
    UsersResponse toUsersResponse(Users users);

    List<UsersResponse> toUsersResponseList(List<Users> users);

    @Mapping(source = "userId", target = "id") // Map từ UUID userId sang String id
        // username, email, avatar, isOnline tự động map
    FriendListResponse toFriendListResponse(Users users);
}
