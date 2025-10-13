package com.whispervault.DTO.UserDTO;

public record UserProfile(
        Integer Id,
        String email,
        String username,
        String alias,
        Integer messages) {
}
