package com.allinone.entity;

import com.allinone.constrant.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @Column(name = "password", unique = true, nullable = false)
    String password;

    @Column(name = "role")
    Role role;
}
