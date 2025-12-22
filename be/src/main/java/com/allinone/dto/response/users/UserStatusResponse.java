package com.allinone.dto.response.users;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
public class UserStatusResponse {
    private String userId;
    private boolean isOnline;
}