package com.worldcup.domain.tournament;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TournamentPredictionMapper {

    @Mapping(source = "user.id",                               target = "userId")
    @Mapping(source = "predictedWinner.id",                    target = "predictedWinnerId")
    @Mapping(source = "predictedWinner.country.name",          target = "predictedWinnerName")
    @Mapping(source = "predictedWinner.country.flagEmoji",     target = "predictedWinnerFlag")
    @Mapping(source = "predictedRunnerUp.id",                  target = "predictedRunnerUpId")
    @Mapping(source = "predictedRunnerUp.country.name",        target = "predictedRunnerUpName")
    @Mapping(source = "predictedRunnerUp.country.flagEmoji",   target = "predictedRunnerUpFlag")
    @Mapping(source = "predictedTopScorer.id",                 target = "predictedTopScorerId")
    @Mapping(source = "predictedTopScorer.name",               target = "predictedTopScorerName")
    @Mapping(source = "predictedTopScorer.team.country.name",  target = "predictedTopScorerTeam")
    TournamentPredictionDto toDto(TournamentPrediction tp);
}
