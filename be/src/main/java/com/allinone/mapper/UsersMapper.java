package com.allinone.mapper;

import com.allinone.dto.response.users.UsersResponse;
import com.allinone.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsersMapper {

    @Mapping(target = "role", expression = "java(users.getRole().name())")
    UsersResponse toUsersResponse(Users users);
}
