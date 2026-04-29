package com.worldcup.domain.prediction;

import com.worldcup.domain.community.CommunityStatsDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    @EntityGraph(attributePaths = {"user", "match"})
    Optional<Prediction> findByUserIdAndMatchId(Long userId, Long matchId);

    boolean existsByUserIdAndMatchId(Long userId, Long matchId);

    @EntityGraph(attributePaths = {"user", "match"})
    List<Prediction> findByMatchId(Long matchId);

    @EntityGraph(attributePaths = {"user", "match"})
    List<Prediction> findByUserId(Long userId);

    @Query(nativeQuery = true, value = """
            SELECT p.match_id AS matchId,
              CAST(ROUND(100.0 * SUM(CASE WHEN p.predicted_home_score > p.predicted_away_score THEN 1 ELSE 0 END) / COUNT(*)) AS int) AS homePct,
              CAST(ROUND(100.0 * SUM(CASE WHEN p.predicted_home_score = p.predicted_away_score THEN 1 ELSE 0 END) / COUNT(*)) AS int) AS drawPct,
              CAST(ROUND(100.0 * SUM(CASE WHEN p.predicted_home_score < p.predicted_away_score THEN 1 ELSE 0 END) / COUNT(*)) AS int) AS awayPct,
              CAST(COUNT(*) AS int) AS totalPredictions
            FROM predictions p
            JOIN matches m ON p.match_id = m.id
            WHERE m.status::text IN ('LIVE','FINISHED')
            GROUP BY p.match_id
            ORDER BY totalPredictions DESC
            LIMIT 10
            """)
    List<CommunityStatsDto.MatchConsensus> findMatchConsensus();
}
