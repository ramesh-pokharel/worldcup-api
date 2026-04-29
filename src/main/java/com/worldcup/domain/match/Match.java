package com.worldcup.domain.match;

import com.worldcup.domain.reference.Stadium;
import com.worldcup.domain.team.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "matches")
@Getter
@NoArgsConstructor
public class Match {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer matchNumber;

    /*
     * WHY @Column(columnDefinition): PostgreSQL won't cast varchar → custom enum type implicitly.
     * Declaring the exact DB type name makes Hibernate use the correct JDBC type on both reads and writes.
     */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "match_stage")
    private MatchStage stage;

    private String groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team team1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team team2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    private OffsetDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "match_status")
    private MatchStatus status;

    @Column(name = "home_score")
    private Integer team1Score;

    @Column(name = "away_score")
    private Integer team2Score;

    @Column(name = "home_score_pen")
    private Integer team1ScorePen;

    @Column(name = "away_score_pen")
    private Integer team2ScorePen;

    private Integer attendance;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
