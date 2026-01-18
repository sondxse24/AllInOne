package com.allinone.repository.iam;

import com.allinone.entity.iam.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendRepository extends JpaRepository<Friendship, UUID> {

    @Query("SELECT f FROM Friendship f WHERE f.addresseeId = :addresseeId AND f.status = com.allinone.constrant.FriendStatus.PENDING")
    List<Friendship> findAllPendingRequests(@Param("addresseeId") String addresseeId);

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requesterId = :u1 AND f.addresseeId = :u2) OR " +
            "(f.requesterId = :u2 AND f.addresseeId = :u1)")
    Optional<Friendship> findRelation(@Param("u1") String u1, @Param("u2") String u2);

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requesterId = :id OR f.addresseeId = :id) " +
            "AND f.status = com.allinone.constrant.FriendStatus.ACCEPTED")
    List<Friendship> findAcceptedFriends(@Param("id") String id);

    // Thêm hàm này nếu bạn dùng trong getListFriend cũ
    List<Friendship> findByRequesterId(String requesterId);
}