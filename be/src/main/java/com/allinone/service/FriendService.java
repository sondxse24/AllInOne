package com.allinone.service;

import com.allinone.dto.request.friend.AddFriendRequest;
import com.allinone.dto.request.friend.DeleteFriendRequest;
import com.allinone.dto.request.friend.ResponseAddFriendRequest;
import com.allinone.dto.response.friend.FriendListResponse;
import com.allinone.dto.response.friend.FriendResponse;
import com.allinone.dto.response.users.UsersResponse;

import java.util.List;

public interface FriendService {
    void addFriend(AddFriendRequest friend);
    List<FriendResponse> getAddFriendList();
    void acceptFriend(ResponseAddFriendRequest response);
    void deleteFriend(DeleteFriendRequest deleteFriendRequest);
    List<FriendListResponse> getListFriend();
}
