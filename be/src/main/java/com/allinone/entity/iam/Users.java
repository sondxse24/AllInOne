package com.allinone.entity.iam;

import com.allinone.constrant.AuthProvider;
import com.allinone.constrant.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID userId;

    @Column(name = "numerical_order", unique = true, columnDefinition = "serial", insertable = false, updatable = false)
    Long numericalOrder;

    @Column(name = "username", nullable = false)
    String username;

    @Column(name = "email", unique = true, nullable = false)
    String email;

    @Column(name = "password", nullable = false)
    String password;

    @Column(name = "avatar", columnDefinition = "TEXT")
    String avatar;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    AuthProvider provider;

    @Column(name = "enabled")
    @Builder.Default
    boolean enabled = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
