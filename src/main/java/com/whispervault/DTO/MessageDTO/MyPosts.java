package com.whispervault.DTO.MessageDTO;

import java.time.LocalDateTime;

public record MyPosts(
        Integer messageId,
        String title,
        String content,
        LocalDateTime createdAt,
        Boolean edited
) {
}
