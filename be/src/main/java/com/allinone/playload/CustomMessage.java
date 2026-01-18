package com.allinone.playload;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PACKAGE)
public class CustomMessage {
    String message;
}
