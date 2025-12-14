package com.allinone.serviceImpl;

import com.allinone.constrant.Role;
import com.allinone.dto.request.users.CreateUsersRequest;
import com.allinone.dto.response.users.UsersResponse;
import com.allinone.entity.Users;
import com.allinone.mapper.UsersMapper;
import com.allinone.repository.UsersRepository;
import com.allinone.service.UsersService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    UsersRepository usersRepository;
    UsersMapper usersMapper;
    PasswordEncoder passwordEncoder;

    public UsersResponse createUser(CreateUsersRequest createUser) {

        Role role = Role.MEMBER;

        if (createUser.getRole() != null) {
            role = Role.valueOf(createUser.getRole());
        }

        Users user = Users.builder()
                .username(createUser.getName())
                .email(createUser.getEmail())
                .password(passwordEncoder.encode(createUser.getPassword()))
                .role(role)
                .build();

        return usersMapper.toUsersResponse(usersRepository.save(user));
    }

    @Override
    public UsersResponse findUserByNumericalOrder(long numerical_order) {
        Users user = usersRepository.findUsersByNumericalOrder(numerical_order);
        return usersMapper.toUsersResponse(user);
    }
}
