package com.worldcup.domain.prediction;

import com.worldcup.AbstractRepositoryTest;
import com.worldcup.TestDataFactory;
import com.worldcup.domain.match.Match;
import com.worldcup.domain.match.MatchRepository;
import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionRepositoryTest extends AbstractRepositoryTest {

    @Autowired PredictionRepository predRepo;
    @Autowired UserRepository userRepo;
    @Autowired MatchRepository matchRepo;
    @PersistenceContext EntityManager em;

    private User user1, user2;
    private Match match1, match2;

    @BeforeEach
    void setUp() {
        user1  = userRepo.save(TestDataFactory.user("pred1").build());
        user2  = userRepo.save(TestDataFactory.user("pred2").build());
        List<Match> matches = matchRepo.findAllByOrderByScheduledAtAsc();
        match1 = matches.get(0);
        match2 = matches.get(1);
    }

    private Prediction save(User user, Match match, int s1, int s2) {
        return predRepo.save(Prediction.builder()
                .user(user).match(match)
                .predictedTeam1Score(s1).predictedTeam2Score(s2)
                .pointsEarned(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
    }

    // ---- existsByUserIdAndMatchId ----

    @Test
    void existsByUserIdAndMatchId_trueAfterSave_falseBeforeSave() {
        assertThat(predRepo.existsByUserIdAndMatchId(user1.getId(), match1.getId())).isFalse();
        save(user1, match1, 2, 1);
        em.flush(); em.clear();
        assertThat(predRepo.existsByUserIdAndMatchId(user1.getId(), match1.getId())).isTrue();
    }

    // ---- findByUserIdAndMatchId ----

    @Test
    void findByUserIdAndMatchId_returnsCorrectPrediction() {
        Prediction saved = save(user1, match1, 3, 0);
        em.flush(); em.clear();

        Prediction found = predRepo.findByUserIdAndMatchId(user1.getId(), match1.getId()).orElseThrow();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getPredictedTeam1Score()).isEqualTo(3);
        assertThat(found.getPredictedTeam2Score()).isEqualTo(0);
    }

    @Test
    void findByUserIdAndMatchId_eagerlyLoadsUserAndMatch() {
        save(user1, match1, 1, 1);
        em.flush(); em.clear();

        Prediction found = predRepo.findByUserIdAndMatchId(user1.getId(), match1.getId()).orElseThrow();
        // These would throw LazyInitializationException without @EntityGraph
        assertThat(found.getUser().getEmail()).isNotBlank();
        assertThat(found.getMatch().getId()).isEqualTo(match1.getId());
    }

    @Test
    void findByUserIdAndMatchId_wrongUser_returnsEmpty() {
        save(user1, match1, 1, 0);
        em.flush(); em.clear();
        assertThat(predRepo.findByUserIdAndMatchId(user2.getId(), match1.getId())).isEmpty();
    }

    // ---- findByMatchId ----

    @Test
    void findByMatchId_returnsAllPredictionsForThatMatch() {
        save(user1, match1, 2, 0);
        save(user2, match1, 1, 1);
        em.flush(); em.clear();

        List<Prediction> preds = predRepo.findByMatchId(match1.getId());
        assertThat(preds).hasSize(2);
        assertThat(preds).extracting(p -> p.getUser().getId())
                .containsExactlyInAnyOrder(user1.getId(), user2.getId());
    }

    @Test
    void findByMatchId_doesNotReturnOtherMatchPredictions() {
        save(user1, match1, 2, 0);
        save(user1, match2, 1, 0);
        em.flush(); em.clear();

        List<Prediction> preds = predRepo.findByMatchId(match1.getId());
        assertThat(preds).hasSize(1);
        assertThat(preds.get(0).getMatch().getId()).isEqualTo(match1.getId());
    }

    // ---- findByUserId ----

    @Test
    void findByUserId_returnsAllPredictionsForThatUser() {
        save(user1, match1, 2, 0);
        save(user1, match2, 0, 1);
        em.flush(); em.clear();

        List<Prediction> preds = predRepo.findByUserId(user1.getId());
        assertThat(preds).hasSize(2);
        assertThat(preds).allMatch(p -> p.getUser().getId().equals(user1.getId()));
    }

    @Test
    void findByUserId_doesNotReturnOtherUserPredictions() {
        save(user1, match1, 2, 0);
        save(user2, match1, 1, 0);
        em.flush(); em.clear();

        assertThat(predRepo.findByUserId(user1.getId())).hasSize(1);
        assertThat(predRepo.findByUserId(user2.getId())).hasSize(1);
    }

    // ---- PredictionLeaderboard ----

    @Autowired PredictionLeaderboardRepository leaderboardRepo;

    @Test
    void leaderboard_orderedByTotalPointsDescThenExactScoresDesc() {
        User u3 = userRepo.save(TestDataFactory.user("lb3").build());
        User u4 = userRepo.save(TestDataFactory.user("lb4").build());

        leaderboardRepo.save(PredictionLeaderboard.builder()
                .userId(u3.getId()).user(u3).totalPoints(10).exactScores(2).correctResults(4)
                .updatedAt(OffsetDateTime.now()).build());
        leaderboardRepo.save(PredictionLeaderboard.builder()
                .userId(u4.getId()).user(u4).totalPoints(10).exactScores(3).correctResults(1)
                .updatedAt(OffsetDateTime.now()).build());
        em.flush(); em.clear();

        List<PredictionLeaderboard> top = leaderboardRepo.findTop50ByOrderByTotalPointsDescExactScoresDesc();
        // u4 has same total points but more exact scores — should rank first
        List<Long> topIds = top.stream().map(PredictionLeaderboard::getUserId).toList();
        assertThat(topIds.indexOf(u4.getId())).isLessThan(topIds.indexOf(u3.getId()));
    }
}
