package com.worldcup.domain.player;

import com.worldcup.domain.community.CommunityStatsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    @EntityGraph(attributePaths = {"team", "team.country"})
    Page<Player> findByTeamIdOrderByPositionAscShirtNumberAsc(Long teamId, Pageable pageable);

    @EntityGraph(attributePaths = {"team", "team.country"})
    Optional<Player> findById(Long id);

    @EntityGraph(attributePaths = {"team", "team.country"})
    @Query("SELECT p FROM Player p WHERE p IN (SELECT fp FROM User u JOIN u.favoritePlayers fp WHERE u.id = :userId) ORDER BY p.team.country.name, p.position")
    List<Player> findFavoritesByUserId(Long userId);

    @Query(nativeQuery = true, value = """
            SELECT p.id AS playerId, p.name,
                   c.name AS teamName, c.code_iso2 AS codeIso2,
                   COUNT(*) AS favCount
            FROM user_favorite_players ufp
            JOIN players p   ON ufp.player_id  = p.id
            JOIN teams t     ON p.team_id       = t.id
            JOIN countries c ON t.country_id    = c.id
            GROUP BY p.id, p.name, c.name, c.code_iso2
            ORDER BY favCount DESC
            LIMIT 5
            """)
    List<CommunityStatsDto.PlayerFavStat> findTopFavoritePlayers();
}
