package com.whispervault.DTO.MessageDTO;

public record EditMessage(
        Integer messageId,
        String title,
        String content
) {}