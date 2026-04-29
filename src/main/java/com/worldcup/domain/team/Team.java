package com.worldcup.domain.team;

import com.worldcup.domain.reference.Country;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "teams")
@Getter
@NoArgsConstructor
public class Team {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    private Integer fifaRanking;
    private String groupId;
    private String manager;
    private String kitPrimary;
    private String kitSecondary;
    private LocalDate qualifiedAt;
    private Boolean isHost;
    private OffsetDateTime createdAt;
}
