package com.worldcup.domain.tournament;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TournamentResultRepository extends JpaRepository<TournamentResult, Long> {

    @EntityGraph(attributePaths = {"actualWinner", "actualTopScorer"})
    Optional<TournamentResult> findTopByOrderBySetAtDesc();
}
