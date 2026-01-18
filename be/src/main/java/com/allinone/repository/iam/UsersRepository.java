package com.allinone.repository.iam;

import com.allinone.dto.response.users.UsersResponse;
import com.allinone.entity.iam.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByEmail(String email);
    Users findUsersByNumericalOrder(long numerical_order);

    boolean existsByEmail(String email);

    Users findByUserId(UUID userId);

    List<Users> findByUsername(String username);
}
