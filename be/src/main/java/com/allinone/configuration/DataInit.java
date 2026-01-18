package com.allinone.configuration;

import com.allinone.constrant.Role;
import com.allinone.entity.iam.Users;
import com.allinone.repository.iam.UsersRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataInit implements CommandLineRunner {

    UsersRepository usersRepository;
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usersRepository.count() == 0) {

            List<Users> users = new ArrayList<>();

            Users user1 = Users.builder()
                    .email("a@gmail.com")
                    .username("a")
                    .password(passwordEncoder.encode("1"))
                    .role(Role.MEMBER)
                    .enabled(true)
                    .numericalOrder(1L)
                    .build();
            users.add(user1);

            Users user2 = Users.builder()
                    .email("b@gmail.com")
                    .username("b")
                    .password(passwordEncoder.encode("1"))
                    .role(Role.MEMBER)
                    .enabled(true)
                    .numericalOrder(2L)
                    .build();
            users.add(user2);

            Users user3 = Users.builder()
                    .email("c@gmail.com")
                    .username("c")
                    .password(passwordEncoder.encode("1"))
                    .role(Role.MEMBER)
                    .enabled(true)
                    .numericalOrder(3L)
                    .build();
            users.add(user3);
            usersRepository.saveAll(users);
        }
    }
}
