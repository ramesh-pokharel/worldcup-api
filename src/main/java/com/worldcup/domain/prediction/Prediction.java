package com.worldcup.domain.prediction;

import com.worldcup.domain.match.Match;
import com.worldcup.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "predictions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prediction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "predicted_home_score")
    private Integer predictedTeam1Score;

    @Column(name = "predicted_away_score")
    private Integer predictedTeam2Score;
    private Integer pointsEarned;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
