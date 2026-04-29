package com.worldcup.domain.match;

import com.worldcup.AbstractRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class MatchRepositoryTest extends AbstractRepositoryTest {

    @Autowired MatchRepository matchRepo;

    // ---- findAll ----

    @Test
    void findAll_returnsSeededMatches() {
        assertThat(matchRepo.findAllByOrderByScheduledAtAsc()).isNotEmpty();
    }

    @Test
    void findAll_eagerlyLoadsTeamsAndStadium_noLazyException() {
        /*
         * This is the highest-risk test: Match has 4 lazy associations (team1, team2,
         * their countries, stadium). A single missing @EntityGraph attribute path
         * throws LazyInitializationException in production when the session closes.
         */
        List<Match> matches = matchRepo.findAllByOrderByScheduledAtAsc();
        assertThatNoException().isThrownBy(() -> {
            for (Match m : matches) {
                if (m.getTeam1() != null) assertThat(m.getTeam1().getCountry().getName()).isNotBlank();
                if (m.getTeam2() != null) assertThat(m.getTeam2().getCountry().getName()).isNotBlank();
                if (m.getStadium() != null) assertThat(m.getStadium().getName()).isNotBlank();
            }
        });
    }

    @Test
    void findAll_orderedByScheduledAt() {
        List<Match> matches = matchRepo.findAllByOrderByScheduledAtAsc();
        for (int i = 1; i < matches.size(); i++) {
            assertThat(matches.get(i).getScheduledAt())
                    .isAfterOrEqualTo(matches.get(i - 1).getScheduledAt());
        }
    }

    // ---- findByStage ----

    @Test
    void findByStage_GROUP_returnsOnlyGroupMatches() {
        List<Match> matches = matchRepo.findByStageOrderByScheduledAtAsc(MatchStage.GROUP);
        assertThat(matches).isNotEmpty();
        assertThat(matches).allMatch(m -> m.getStage() == MatchStage.GROUP);
    }

    // ---- findByGroupId ----

    @Test
    void findByGroupId_returnsOnlyThatGroup() {
        // Grab a group from seed data instead of hardcoding
        String group = matchRepo.findByStageOrderByScheduledAtAsc(MatchStage.GROUP)
                .stream()
                .map(Match::getGroupId)
                .filter(g -> g != null)
                .findFirst()
                .orElseThrow();

        List<Match> matches = matchRepo.findByGroupIdOrderByScheduledAtAsc(group);
        assertThat(matches).isNotEmpty();
        assertThat(matches).allMatch(m -> group.equals(m.getGroupId()));
    }

    // ---- findByStatus ----

    @Test
    void findByStatus_SCHEDULED_returnsOnlyScheduled() {
        List<Match> matches = matchRepo.findByStatusOrderByScheduledAtAsc(MatchStatus.SCHEDULED);
        // All seeded matches are SCHEDULED; result may be empty if seeds use a different status
        assertThat(matches).allMatch(m -> m.getStatus() == MatchStatus.SCHEDULED);
    }

    // ---- findById ----

    @Test
    void findById_loadsAllAssociations() {
        Match any = matchRepo.findAllByOrderByScheduledAtAsc().get(0);
        Match loaded = matchRepo.findById(any.getId()).orElseThrow();

        // team1/team2 can be null for TBD knockout matches — only check when present
        assertThatNoException().isThrownBy(() -> {
            if (loaded.getTeam1() != null) loaded.getTeam1().getCountry().getName();
            if (loaded.getTeam2() != null) loaded.getTeam2().getCountry().getName();
            if (loaded.getStadium() != null) loaded.getStadium().getCity();
        });
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertThat(matchRepo.findById(Long.MAX_VALUE)).isEmpty();
    }

    // ---- Enum mapping (PostgreSQL custom type round-trip) ----

    @Test
    void stage_roundTripsAsEnum_noClassCastException() {
        /*
         * WHY this test: PostgreSQL stores stage as a custom enum type (match_stage).
         * If @Column(columnDefinition) is missing, Hibernate may fail to read it back
         * as a Java enum, throwing ClassCastException at mapping time.
         */
        Match match = matchRepo.findAllByOrderByScheduledAtAsc().get(0);
        assertThat(match.getStage()).isInstanceOf(MatchStage.class);
        assertThat(match.getStatus()).isInstanceOf(MatchStatus.class);
    }
}
