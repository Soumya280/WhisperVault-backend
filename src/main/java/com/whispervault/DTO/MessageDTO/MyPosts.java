package com.whispervault.DTO.MessageDTO;

public record MyPosts(
        Integer messageId,
        String title,
        String content,
        String createdAt,
        Boolean edited
) {
}
