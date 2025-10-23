package com.whispervault.DTO.MessageDTO;

import java.time.LocalDateTime;

public record AllPosts(
        Integer messageId,
        Integer userId,
        String alias,
        String title,
        String content,
        LocalDateTime createdAt,
        Boolean edited
) {}