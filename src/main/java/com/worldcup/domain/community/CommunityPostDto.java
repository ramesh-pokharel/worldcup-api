package com.worldcup.domain.community;

import java.time.OffsetDateTime;

public record CommunityPostDto(
    Long           id,
    Long           userId,
    String         authorName,
    String         body,
    OffsetDateTime createdAt
) {}
