package com.worldcup.domain.match;

import java.time.OffsetDateTime;

public record MatchDto(
    Long id,
    Integer matchNumber,
    String stage,
    String groupId,
    Long team1Id,
    String team1Name,
    String team1Flag,
    String team1FlagCode,
    String team1Kit,
    Long team2Id,
    String team2Name,
    String team2Flag,
    String team2FlagCode,
    String team2Kit,
    String stadiumName,
    String stadiumCity,
    OffsetDateTime scheduledAt,
    String status,
    Integer team1Score,
    Integer team2Score,
    Integer team1ScorePen,
    Integer team2ScorePen
) {}
