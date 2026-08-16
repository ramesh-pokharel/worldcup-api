package com.worldcup.domain.tournament;

import com.worldcup.domain.player.Player;
import com.worldcup.domain.team.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tournament_result")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResult {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_winner_id")
    private Team actualWinner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_top_scorer_id")
    private Player actualTopScorer;

    private OffsetDateTime setAt;
}
