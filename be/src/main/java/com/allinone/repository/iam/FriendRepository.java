package com.allinone.repository.iam;

import com.allinone.entity.iam.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FriendRepository extends JpaRepository<Friendship, UUID> {
}
