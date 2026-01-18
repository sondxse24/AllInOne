package com.allinone.serviceImpl;

import com.allinone.constrant.AuthProvider;
import com.allinone.constrant.Role;
import com.allinone.dto.request.users.CreateUsersRequest;
import com.allinone.dto.response.users.UsersResponse;
import com.allinone.entity.iam.Users;
import com.allinone.exception.AppException;
import com.allinone.exception.ErrorCode;
import com.allinone.mapper.UsersMapper;
import com.allinone.repository.iam.UsersRepository;
import com.allinone.service.UsersService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    UsersRepository usersRepository;
    UsersMapper usersMapper;
    PasswordEncoder passwordEncoder;

    @Transactional
    public UsersResponse createUser(CreateUsersRequest createUser) {

        if (usersRepository.existsByEmail(createUser.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Role role = Role.MEMBER;
        if (createUser.getRole() != null) {
            role = Role.valueOf(createUser.getRole());
        }

        Users user = Users.builder()
                .username(createUser.getName())
                .email(createUser.getEmail())
                .password(passwordEncoder.encode(createUser.getPassword()))
                .role(role)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .avatar(null)
                .build();

        return usersMapper.toUsersResponse(usersRepository.save(user));
    }

    @Override
    @Transactional
    public UsersResponse findUserByNumericalOrder(long numerical_order) {
        Users user = usersRepository.findUsersByNumericalOrder(numerical_order);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return usersMapper.toUsersResponse(user);
    }

    @Override
    @Transactional
    public UsersResponse getMe() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.USER_NOT_AUTHENTICATED);
        }

        Users users = usersRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return usersMapper.toUsersResponse(users);
    }

    @Override
    public List<UsersResponse> getAllUsers() {
        return usersMapper.toUsersResponseList(usersRepository.findAll());
    }

    @Override
    public List<UsersResponse> getAllUsersByName(String username) {
        return usersMapper.toUsersResponseList(usersRepository.findByUsername(username));
    }

    @Override
    public Users findByEmail(String email) {
        return usersRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public Users findByUserId(UUID id) {
        return usersRepository.findByUserId(id);
    }
}
