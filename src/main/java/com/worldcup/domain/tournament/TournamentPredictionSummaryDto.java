package com.worldcup.domain.tournament;

public record TournamentPredictionSummaryDto(
    Long   userId,
    String displayName,
    String predictedWinnerName,
    String predictedWinnerFlagCode,
    String predictedRunnerUpName,
    String predictedRunnerUpFlagCode,
    String predictedTopScorerName,
    String predictedTopScorerTeam,
    Short  pointsEarned
) {}
