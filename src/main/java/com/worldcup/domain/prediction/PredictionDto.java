package com.worldcup.domain.prediction;

import java.time.OffsetDateTime;

public record PredictionDto(
    Long id,
    Long userId,
    String authorName,
    Long matchId,
    Integer predictedTeam1Score,
    Integer predictedTeam2Score,
    Integer pointsEarned,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
