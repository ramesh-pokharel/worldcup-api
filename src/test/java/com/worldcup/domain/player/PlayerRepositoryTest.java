package com.worldcup.domain.player;

import com.worldcup.AbstractRepositoryTest;
import com.worldcup.TestDataFactory;
import com.worldcup.domain.team.Team;
import com.worldcup.domain.team.TeamRepository;
import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class PlayerRepositoryTest extends AbstractRepositoryTest {

    @Autowired PlayerRepository playerRepo;
    @Autowired TeamRepository teamRepo;
    @Autowired UserRepository userRepo;
    @PersistenceContext EntityManager em;

    private Team anyTeam() {
        return teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc().get(0);
    }

    // ---- findByTeamId (paginated) ----

    @Test
    void findByTeamId_returnsPlayersForThatTeam() {
        Team team = anyTeam();
        Page<Player> page = playerRepo.findByTeamIdOrderByPositionAscShirtNumberAsc(
                team.getId(), PageRequest.of(0, 30));

        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent()).allMatch(p -> p.getTeam().getId().equals(team.getId()));
    }

    @Test
    void findByTeamId_eagerlyLoadsTeamAndCountry_noLazyException() {
        Team team = anyTeam();
        Page<Player> page = playerRepo.findByTeamIdOrderByPositionAscShirtNumberAsc(
                team.getId(), PageRequest.of(0, 30));

        assertThatNoException().isThrownBy(() -> {
            for (Player p : page.getContent()) {
                assertThat(p.getTeam().getCountry().getName()).isNotBlank();
                assertThat(p.getTeam().getCountry().getFlagEmoji()).isNotNull();
            }
        });
    }

    @Test
    void findByTeamId_pagination_secondPageHasFewerElements() {
        Team team = anyTeam();
        int pageSize = 5;
        long total = playerRepo.findByTeamIdOrderByPositionAscShirtNumberAsc(
                team.getId(), PageRequest.of(0, pageSize)).getTotalElements();

        if (total > pageSize) {
            Page<Player> page2 = playerRepo.findByTeamIdOrderByPositionAscShirtNumberAsc(
                    team.getId(), PageRequest.of(1, pageSize));
            assertThat(page2.getContent()).isNotEmpty();
            assertThat(page2.getNumber()).isEqualTo(1);
        }
    }

    @Test
    void findByTeamId_unknownTeam_returnsEmptyPage() {
        Page<Player> page = playerRepo.findByTeamIdOrderByPositionAscShirtNumberAsc(
                Long.MAX_VALUE, PageRequest.of(0, 30));
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    // ---- findById ----

    @Test
    void findById_loadsTeamAndCountry() {
        Team team = anyTeam();
        Player player = playerRepo.findByTeamIdOrderByPositionAscShirtNumberAsc(
                team.getId(), PageRequest.of(0, 1)).getContent().get(0);

        Player loaded = playerRepo.findById(player.getId()).orElseThrow();
        assertThat(loaded.getTeam()).isNotNull();
        assertThat(loaded.getTeam().getCountry()).isNotNull();
        assertThat(loaded.getTeam().getCountry().getName()).isNotBlank();
    }

    // ---- findFavoritesByUserId ----

    @Test
    void findFavoritesByUserId_returnsOnlyFavoritedPlayers() {
        User user = userRepo.save(TestDataFactory.user("playerfan").build());
        Team team = anyTeam();
        Player player = playerRepo.findByTeamIdOrderByPositionAscShirtNumberAsc(
                team.getId(), PageRequest.of(0, 1)).getContent().get(0);

        User withFav = userRepo.findByIdWithFavoritePlayers(user.getId()).orElseThrow();
        withFav.getFavoritePlayers().add(player);
        userRepo.save(withFav);
        em.flush();
        em.clear();

        List<Player> favorites = playerRepo.findFavoritesByUserId(user.getId());
        assertThat(favorites).hasSize(1);
        assertThat(favorites.get(0).getId()).isEqualTo(player.getId());
    }

    @Test
    void findFavoritesByUserId_noFavorites_returnsEmpty() {
        User user = userRepo.save(TestDataFactory.user("nofav_player").build());
        assertThat(playerRepo.findFavoritesByUserId(user.getId())).isEmpty();
    }
}
