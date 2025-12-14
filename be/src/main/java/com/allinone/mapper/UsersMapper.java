package com.allinone.mapper;

import com.allinone.dto.response.users.UsersResponse;
import com.allinone.entity.Users;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsersMapper {

    UsersResponse toUsersResponse(Users users);
}
