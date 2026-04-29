package com.worldcup.domain.user;

public record UserDto(
    Long id,
    String email,
    String displayName,
    String avatarUrl,
    String role
) {}
