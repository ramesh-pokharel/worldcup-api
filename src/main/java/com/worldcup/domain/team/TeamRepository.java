package com.worldcup.domain.team;

import com.worldcup.domain.community.CommunityStatsDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    /*
     * WHY @EntityGraph: Team.country and country.confederation are LAZY by default.
     * Without this, mapping to TeamDto would trigger N+1 queries (one extra SELECT per team).
     * @EntityGraph joins them in a single SQL query.
     */
    @EntityGraph(attributePaths = {"country", "country.confederation"})
    List<Team> findAllByOrderByGroupIdAscFifaRankingAsc();

    @EntityGraph(attributePaths = {"country", "country.confederation"})
    List<Team> findByGroupIdIgnoreCaseOrderByFifaRankingAsc(String groupId);

    @EntityGraph(attributePaths = {"country", "country.confederation"})
    Optional<Team> findById(Long id);

    @EntityGraph(attributePaths = {"country", "country.confederation"})
    @Query("SELECT t FROM Team t WHERE t IN (SELECT ft FROM User u JOIN u.favoriteTeams ft WHERE u.id = :userId) ORDER BY t.country.name")
    List<Team> findFavoritesByUserId(Long userId);

    @Query(nativeQuery = true, value = """
            SELECT t.id AS teamId, c.name AS countryName,
                   c.flag_emoji AS flagEmoji, c.code_iso2 AS codeIso2,
                   COUNT(*) AS favCount
            FROM user_favorite_teams uft
            JOIN teams t     ON uft.team_id    = t.id
            JOIN countries c ON t.country_id   = c.id
            GROUP BY t.id, c.name, c.flag_emoji, c.code_iso2
            ORDER BY favCount DESC
            LIMIT 5
            """)
    List<CommunityStatsDto.TeamFavStat> findTopFavoriteTeams();
}
