package com.worldcup.domain.prediction;

public record LeaderboardDto(
    Long userId,
    String displayName,
    String avatarUrl,
    Integer totalPoints,
    Integer exactScores,
    Integer correctResults,
    Integer tournamentPoints
) {}
