package com.allinone.service;

import com.allinone.dto.request.friend.AddFriendRequest;
import com.allinone.dto.request.friend.DeleteFriendRequest;
import com.allinone.dto.request.friend.ResponseAddFriendRequest;

public interface FriendService {
    void addFriend(AddFriendRequest friend);
    void acceptFriend(ResponseAddFriendRequest response);
    void deleteFriend(DeleteFriendRequest deleteFriendRequest);
}
