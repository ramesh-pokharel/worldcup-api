package com.worldcup.domain.comment;

import java.time.OffsetDateTime;

public record CommentDto(
    Long id,
    Long userId,
    String authorName,
    String authorAvatar,
    Long matchId,
    Long teamId,
    Long playerId,
    Long parentId,
    String body,
    Boolean deleted,
    Integer likeCount,
    Boolean likedByMe,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
