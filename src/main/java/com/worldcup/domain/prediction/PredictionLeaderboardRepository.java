package com.worldcup.domain.prediction;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PredictionLeaderboardRepository extends JpaRepository<PredictionLeaderboard, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<PredictionLeaderboard> findTop5ByOrderByTotalPointsDescExactScoresDesc();
}
