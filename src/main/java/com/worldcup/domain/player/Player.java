package com.worldcup.domain.player;

import com.worldcup.domain.team.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "players")
@Getter
@NoArgsConstructor
public class Player {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private String name;
    private LocalDate dateOfBirth;
    private String nationality;
    private String position;
    private Integer shirtNumber;
    private String club;
    private Integer caps;
    private Integer goals;
    private Integer heightCm;
    private Long marketValueEur;
    private Boolean isCaptain;
    private OffsetDateTime createdAt;
}
