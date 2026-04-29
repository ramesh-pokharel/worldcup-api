package com.worldcup.domain.user;

import java.time.OffsetDateTime;

public record AdminUserDto(
    Long id,
    String email,
    String role,
    Boolean isActive,
    Boolean isVerified,
    OffsetDateTime createdAt,
    OffsetDateTime lastLoginAt
) {}
