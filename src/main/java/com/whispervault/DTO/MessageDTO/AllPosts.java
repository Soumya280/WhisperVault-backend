package com.whispervault.DTO.MessageDTO;

public record AllPosts(
        Integer messageId,
        Integer userId,
        String alias,
        String title,
        String content,
        String createdAt,
        Boolean edited
) {}