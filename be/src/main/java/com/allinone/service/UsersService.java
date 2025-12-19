package com.allinone.service;

import com.allinone.dto.request.users.CreateUsersRequest;
import com.allinone.dto.response.users.UsersResponse;

import java.util.List;

public interface UsersService {

    UsersResponse createUser(CreateUsersRequest users);

    UsersResponse findUserByNumericalOrder(long numerical_order);

    UsersResponse getMe();

    List<UsersResponse> getAllUsers();
}
