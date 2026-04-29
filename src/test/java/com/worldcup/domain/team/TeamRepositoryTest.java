package com.worldcup.domain.team;

import com.worldcup.AbstractRepositoryTest;
import com.worldcup.TestDataFactory;
import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class TeamRepositoryTest extends AbstractRepositoryTest {

    @Autowired TeamRepository teamRepo;
    @Autowired UserRepository userRepo;
    @PersistenceContext EntityManager em;

    // ---- findAll ----

    @Test
    void findAll_returnsSeededTeams() {
        List<Team> teams = teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc();
        assertThat(teams).isNotEmpty();
    }

    @Test
    void findAll_eagerlyLoadsCountryAndConfederation_noLazyException() {
        /*
         * WHY this test matters: if @EntityGraph is misconfigured, accessing
         * country.confederation outside a session throws LazyInitializationException.
         * Running inside @Transactional keeps the session open, but this would
         * still catch missing JOIN FETCH in the query itself.
         */
        List<Team> teams = teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc();
        assertThatNoException().isThrownBy(() -> {
            for (Team t : teams) {
                String name = t.getCountry().getName();
                String conf = t.getCountry().getConfederation().getName();
                assertThat(name).isNotBlank();
                assertThat(conf).isNotBlank();
            }
        });
    }

    @Test
    void findAll_orderedByGroupThenRanking() {
        List<Team> teams = teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc();
        // Within any group, FIFA ranking must be non-decreasing
        String lastGroup = null;
        Integer lastRank = null;
        for (Team t : teams) {
            if (!t.getGroupId().equals(lastGroup)) {
                lastGroup = t.getGroupId();
                lastRank = null;
            }
            if (lastRank != null && t.getFifaRanking() != null) {
                assertThat(t.getFifaRanking()).isGreaterThanOrEqualTo(lastRank);
            }
            lastRank = t.getFifaRanking();
        }
    }

    // ---- findByGroupId ----

    @Test
    void findByGroupId_returnsOnlyThatGroup() {
        // Pick the first group from seeded data rather than hardcoding "A"
        String group = teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc()
                .get(0).getGroupId();

        List<Team> teams = teamRepo.findByGroupIdIgnoreCaseOrderByFifaRankingAsc(group);

        assertThat(teams).isNotEmpty();
        assertThat(teams).allMatch(t -> group.equalsIgnoreCase(t.getGroupId()));
    }

    @Test
    void findByGroupId_caseInsensitive() {
        String group = teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc()
                .get(0).getGroupId();

        List<Team> upper = teamRepo.findByGroupIdIgnoreCaseOrderByFifaRankingAsc(group.toUpperCase());
        List<Team> lower = teamRepo.findByGroupIdIgnoreCaseOrderByFifaRankingAsc(group.toLowerCase());

        assertThat(upper.stream().map(Team::getId).toList())
                .containsExactlyElementsOf(lower.stream().map(Team::getId).toList());
    }

    @Test
    void findByGroupId_unknownGroup_returnsEmpty() {
        List<Team> teams = teamRepo.findByGroupIdIgnoreCaseOrderByFifaRankingAsc("ZZZ");
        assertThat(teams).isEmpty();
    }

    // ---- findById ----

    @Test
    void findById_loadsCountryAndConfederation() {
        Long id = teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc().get(0).getId();
        Optional<Team> team = teamRepo.findById(id);

        assertThat(team).isPresent();
        assertThat(team.get().getCountry()).isNotNull();
        assertThat(team.get().getCountry().getConfederation()).isNotNull();
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertThat(teamRepo.findById(Long.MAX_VALUE)).isEmpty();
    }

    // ---- findFavoritesByUserId ----

    @Test
    void findFavoritesByUserId_returnsOnlyFavoritedTeams() {
        User user = userRepo.save(TestDataFactory.user("teamfan").build());
        Team team = teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc().get(0);

        User withFav = userRepo.findByIdWithFavoriteTeams(user.getId()).orElseThrow();
        withFav.getFavoriteTeams().add(team);
        userRepo.save(withFav);
        em.flush();
        em.clear(); // clear L1 cache so the next query hits the database

        List<Team> favorites = teamRepo.findFavoritesByUserId(user.getId());
        assertThat(favorites).hasSize(1);
        assertThat(favorites.get(0).getId()).isEqualTo(team.getId());
    }

    @Test
    void findFavoritesByUserId_noFavorites_returnsEmpty() {
        User user = userRepo.save(TestDataFactory.user("nofav").build());
        assertThat(teamRepo.findFavoritesByUserId(user.getId())).isEmpty();
    }
}
