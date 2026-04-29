package com.worldcup.domain.tournament;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TournamentPredictionRepository extends JpaRepository<TournamentPrediction, Long> {

    @EntityGraph(attributePaths = {
        "predictedWinner", "predictedWinner.country",
        "predictedRunnerUp", "predictedRunnerUp.country",
        "predictedTopScorer", "predictedTopScorer.team", "predictedTopScorer.team.country"
    })
    Optional<TournamentPrediction> findByUserId(Long userId);
}
