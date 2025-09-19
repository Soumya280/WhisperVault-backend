package com.whispervault.DTO.MessageDTO;

public record EditedMessageResponse(
        Integer messageId,
        String title,
        String content,
        Boolean edited,
        String username) {
}
