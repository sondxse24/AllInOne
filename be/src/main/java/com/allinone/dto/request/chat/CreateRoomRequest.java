package com.allinone.dto.request.chat;

import lombok.Data;
import java.util.List;

@Data
public class CreateRoomRequest {
    private String name;
    private List<String> participantIds;
}