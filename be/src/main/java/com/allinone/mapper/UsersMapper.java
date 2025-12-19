package com.allinone.mapper;

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
}
