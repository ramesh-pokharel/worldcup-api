package com.worldcup.domain.tournament;

import com.worldcup.AbstractRepositoryTest;
import com.worldcup.TestDataFactory;
import com.worldcup.domain.player.Player;
import com.worldcup.domain.player.PlayerRepository;
import com.worldcup.domain.team.Team;
import com.worldcup.domain.team.TeamRepository;
import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class TournamentPredictionRepositoryTest extends AbstractRepositoryTest {

    @Autowired TournamentPredictionRepository tpRepo;
    @Autowired UserRepository userRepo;
    @Autowired TeamRepository teamRepo;
    @Autowired PlayerRepository playerRepo;
    @PersistenceContext EntityManager em;

    private User user;
    private Team team1, team2;
    private Player topScorer;

    @BeforeEach
    void setUp() {
        user   = userRepo.save(TestDataFactory.user("tp").build());
        team1  = teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc().get(0);
        team2  = teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc().get(1);
        topScorer = playerRepo.findByTeamIdOrderByPositionAscShirtNumberAsc(
                team1.getId(), PageRequest.of(0, 1)).getContent().get(0);
    }

    private TournamentPrediction savedPrediction() {
        return tpRepo.save(TournamentPrediction.builder()
                .user(user)
                .predictedWinner(team1)
                .predictedRunnerUp(team2)
                .predictedTopScorer(topScorer)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
    }

    // ---- findByUserId ----

    @Test
    void findByUserId_afterSave_returnsPrediction() {
        savedPrediction();
        em.flush(); em.clear();

        assertThat(tpRepo.findByUserId(user.getId())).isPresent();
    }

    @Test
    void findByUserId_noEntry_returnsEmpty() {
        User other = userRepo.save(TestDataFactory.user("notp").build());
        assertThat(tpRepo.findByUserId(other.getId())).isEmpty();
    }

    @Test
    void findByUserId_eagerlyLoadsTeamAndPlayerAssociations_noLazyException() {
        savedPrediction();
        em.flush(); em.clear();

        TournamentPrediction tp = tpRepo.findByUserId(user.getId()).orElseThrow();
        assertThatNoException().isThrownBy(() -> {
            assertThat(tp.getPredictedWinner().getCountry().getName()).isNotBlank();
            assertThat(tp.getPredictedRunnerUp().getCountry().getName()).isNotBlank();
            assertThat(tp.getPredictedTopScorer().getTeam().getCountry().getName()).isNotBlank();
        });
    }

    @Test
    void findByUserId_partialPrediction_nullFieldsAllowed() {
        // A user may predict only the winner without runner-up or top scorer
        tpRepo.save(TournamentPrediction.builder()
                .user(user)
                .predictedWinner(team1)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
        em.flush(); em.clear();

        TournamentPrediction tp = tpRepo.findByUserId(user.getId()).orElseThrow();
        assertThat(tp.getPredictedWinner()).isNotNull();
        assertThat(tp.getPredictedRunnerUp()).isNull();
        assertThat(tp.getPredictedTopScorer()).isNull();
    }

    // ---- Upsert behaviour (update via save with existing id) ----

    @Test
    void save_withExistingId_updatesRatherThanInserts() {
        TournamentPrediction original = savedPrediction();
        em.flush(); em.clear();

        // Simulate PUT: load, mutate winner, save back with same id
        TournamentPrediction updated = TournamentPrediction.builder()
                .id(original.getId())
                .user(user)
                .predictedWinner(team2)   // changed
                .predictedRunnerUp(team1)
                .predictedTopScorer(topScorer)
                .createdAt(original.getCreatedAt())
                .updatedAt(OffsetDateTime.now())
                .build();
        tpRepo.save(updated);
        em.flush(); em.clear();

        assertThat(tpRepo.count()).isEqualTo(1);  // no duplicate row
        assertThat(tpRepo.findByUserId(user.getId()).orElseThrow()
                .getPredictedWinner().getId()).isEqualTo(team2.getId());
    }
}
