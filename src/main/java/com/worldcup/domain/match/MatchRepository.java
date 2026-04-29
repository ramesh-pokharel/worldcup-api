package com.worldcup.domain.match;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @EntityGraph(attributePaths = {
        "team1", "team1.country",
        "team2", "team2.country",
        "stadium"
    })
    List<Match> findAllByOrderByScheduledAtAsc();

    @EntityGraph(attributePaths = {
        "team1", "team1.country",
        "team2", "team2.country",
        "stadium"
    })
    List<Match> findByStageOrderByScheduledAtAsc(MatchStage stage);

    @EntityGraph(attributePaths = {
        "team1", "team1.country",
        "team2", "team2.country",
        "stadium"
    })
    List<Match> findByGroupIdOrderByScheduledAtAsc(String groupId);

    @EntityGraph(attributePaths = {
        "team1", "team1.country",
        "team2", "team2.country",
        "stadium"
    })
    List<Match> findByStatusOrderByScheduledAtAsc(MatchStatus status);

    @EntityGraph(attributePaths = {
        "team1", "team1.country",
        "team2", "team2.country",
        "stadium"
    })
    Optional<Match> findById(Long id);

    /*
     * WHY native query: JPQL SET on a PostgreSQL custom enum column (match_status)
     * requires an explicit ::match_status cast that JPQL cannot express.
     * Native SQL lets us cast directly and avoids Hibernate type-binding issues.
     * Status is passed as a plain String (enum.name()) and cast in SQL.
     */
    @Modifying
    @Query(nativeQuery = true, value = """
        UPDATE matches
        SET status          = :status::match_status,
            home_score      = :team1Score,
            away_score      = :team2Score,
            home_score_pen  = :team1ScorePen,
            away_score_pen  = :team2ScorePen,
            updated_at      = NOW()
        WHERE id = :id
        """)
    void updateResult(
        @org.springframework.data.repository.query.Param("id")            Long    id,
        @org.springframework.data.repository.query.Param("status")        String  status,
        @org.springframework.data.repository.query.Param("team1Score")    Integer team1Score,
        @org.springframework.data.repository.query.Param("team2Score")    Integer team2Score,
        @org.springframework.data.repository.query.Param("team1ScorePen") Integer team1ScorePen,
        @org.springframework.data.repository.query.Param("team2ScorePen") Integer team2ScorePen
    );
}
