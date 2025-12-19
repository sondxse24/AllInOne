package com.allinone.serviceImpl;

import com.allinone.constrant.FriendStatus;
import com.allinone.dto.request.friend.AddFriendRequest;
import com.allinone.dto.request.friend.DeleteFriendRequest;
import com.allinone.dto.request.friend.ResponseAddFriendRequest;
import com.allinone.entity.iam.Friendship;
import com.allinone.entity.iam.Users;
import com.allinone.repository.iam.FriendRepository;
import com.allinone.service.FriendService;
import com.allinone.service.UsersService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    FriendRepository friendRepository;
    UsersService usersService;

    @Override
    public void addFriend(AddFriendRequest friend) {
        Friendship friendship = new Friendship();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        Users user = usersService.findByEmail(authentication.getName());
        friendship.setRequesterId(user.getUserId().toString());
        friendship.setAddresseeId(friend.getAddresseeId());
        friendship.setStatus(FriendStatus.PENDING);
        friendship.setCreatedAt(LocalDateTime.now());
        friendRepository.save(friendship);
    }

    @Override
    public void acceptFriend(ResponseAddFriendRequest request) {
        Friendship friendship = friendRepository.findById(request.getId()).orElse(null);
        assert friendship != null;
        friendship.setStatus(request.getStatus());
        friendRepository.save(friendship);
    }

    @Override
    public void deleteFriend(DeleteFriendRequest deleteFriendRequest) {
        Friendship friendship = friendRepository.findById(deleteFriendRequest.getId()).orElse(null);
        assert friendship != null;
        friendRepository.delete(friendship);
    }
}
