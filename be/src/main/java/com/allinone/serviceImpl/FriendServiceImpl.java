package com.allinone.serviceImpl;

import com.allinone.constrant.FriendStatus;
import com.allinone.dto.request.friend.AddFriendRequest;
import com.allinone.dto.request.friend.DeleteFriendRequest;
import com.allinone.dto.request.friend.ResponseAddFriendRequest;
import com.allinone.dto.response.friend.FriendListResponse;
import com.allinone.dto.response.friend.FriendResponse;
import com.allinone.dto.response.users.UsersResponse;
import com.allinone.entity.iam.Friendship;
import com.allinone.entity.iam.Users;
import com.allinone.exception.AppException;
import com.allinone.exception.ErrorCode;
import com.allinone.mapper.FriendMapper;
import com.allinone.mapper.UsersMapper;
import com.allinone.repository.iam.FriendRepository;
import com.allinone.service.FriendService;
import com.allinone.service.UsersService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    FriendRepository friendRepository;
    FriendMapper friendMapper;
    UsersService usersService;
    UsersMapper usersMapper;

    @Override
    @Transactional
    public void addFriend(AddFriendRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new AppException(ErrorCode.USER_NOT_AUTHENTICATED);

        Users me = usersService.findByEmail(authentication.getName());
        String myId = me.getUserId().toString();
        String targetId = request.getAddresseeId();

        if (targetId == null || targetId.isEmpty())
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Hoặc tạo lỗi INVALID_INPUT

        if (myId.equals(targetId))
            throw new AppException(ErrorCode.CANNOT_ADD_SELF);

        friendRepository.findRelation(myId, targetId).ifPresent(existing -> {
            if (existing.getStatus() == FriendStatus.ACCEPTED)
                throw new AppException(ErrorCode.ALREADY_FRIENDS);
            if (existing.getStatus() == FriendStatus.PENDING)
                throw new AppException(ErrorCode.FRIEND_REQUEST_PENDING);

            friendRepository.delete(existing);
        });

        Friendship friendship = Friendship.builder()
                .requesterId(myId)
                .addresseeId(targetId)
                .status(FriendStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        friendRepository.save(friendship);
    }

    @Override
    public List<FriendResponse> getAddFriendList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        Users currentUser = usersService.findByEmail(authentication.getName());

        List<Friendship> friendships = friendRepository.findAllPendingRequests(currentUser.getUserId().toString());

        return friendships.stream().map(f -> {
            Users requester = usersService.findByUserId(UUID.fromString(f.getRequesterId()));
            return friendMapper.toFriendResponse(f, requester);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void acceptFriend(ResponseAddFriendRequest request) {
        Friendship friendship = friendRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (request.getStatus() == null)
            throw new AppException(ErrorCode.INVALID_FRIEND_STATUS);

        friendship.setStatus(request.getStatus()); // Có thể là ACCEPTED hoặc DECLINED
        friendRepository.save(friendship);
    }

    @Override
    @Transactional
    public void deleteFriend(DeleteFriendRequest request) {
        Friendship friendship = friendRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        friendRepository.delete(friendship);
    }

    @Override
    public List<FriendListResponse> getListFriend() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = usersService.findByEmail(authentication.getName());
        String myId = user.getUserId().toString();

        List<Friendship> friendships = friendRepository.findAcceptedFriends(myId);

        return friendships.stream().map(f -> {
            String friendId = f.getRequesterId().equals(myId) ? f.getAddresseeId() : f.getRequesterId();

            Users friend = usersService.findByUserId(UUID.fromString(friendId));
            return usersMapper.toFriendListResponse(friend);

        }).collect(Collectors.toList());
    }
}