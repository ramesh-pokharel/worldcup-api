package com.worldcup.domain.tournament;

import com.worldcup.domain.player.Player;
import com.worldcup.domain.team.Team;
import com.worldcup.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tournament_predictions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentPrediction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predicted_winner_id")
    private Team predictedWinner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predicted_runner_up")
    private Team predictedRunnerUp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predicted_top_scorer")
    private Player predictedTopScorer;

    private Short pointsEarned;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
