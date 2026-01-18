package com.allinone.service;

import com.allinone.dto.request.users.CreateUsersRequest;
import com.allinone.dto.response.users.UsersResponse;
import com.allinone.entity.iam.Users;

import java.util.List;
import java.util.UUID;

public interface UsersService {

    UsersResponse createUser(CreateUsersRequest users);

    UsersResponse findUserByNumericalOrder(long numerical_order);

    UsersResponse getMe();

    List<UsersResponse> getAllUsers();

    List<UsersResponse> getAllUsersByName(String username);

    Users findByEmail(String name);

    Users findByUserId(UUID id);
}
