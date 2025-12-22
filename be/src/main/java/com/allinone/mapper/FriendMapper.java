package com.allinone.mapper;

import com.allinone.dto.response.friend.FriendResponse;
import com.allinone.entity.iam.Friendship;
import com.allinone.entity.iam.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FriendMapper {

    @Mapping(target = "id", source = "friendship.id")
    @Mapping(target = "requesterName", source = "user.username")
    @Mapping(target = "requesterId", source = "friendship.requesterId")
    @Mapping(target = "addresseeId", source = "friendship.addresseeId")
    @Mapping(target = "createdAt", source = "friendship.createdAt")
    FriendResponse toFriendResponse(Friendship friendship, Users user);

    List<FriendResponse> toFriendResponseList(List<Friendship> friend);
}
