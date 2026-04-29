package com.worldcup.domain.prediction;

import com.worldcup.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "prediction_leaderboard")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionLeaderboard {

    /*
     * WHY @MapsId: prediction_leaderboard.user_id is both the PK and a FK to users.
     * @MapsId tells JPA that the entity's PK value comes from the @OneToOne relationship,
     * so we don't need a separate @GeneratedValue — the userId IS the user's id.
     */
    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private Integer totalPoints;
    private Integer exactScores;
    private Integer correctResults;
    private OffsetDateTime updatedAt;
}
