package com.worldcup.domain.tournament;

public record TournamentPredictionDto(
    Long id,
    Long userId,
    Long predictedWinnerId,
    String predictedWinnerName,
    String predictedWinnerFlag,
    Long predictedRunnerUpId,
    String predictedRunnerUpName,
    String predictedRunnerUpFlag,
    Long predictedTopScorerId,
    String predictedTopScorerName,
    String predictedTopScorerTeam
) {}
