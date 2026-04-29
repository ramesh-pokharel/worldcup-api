package com.worldcup.domain.player;

import java.time.LocalDate;

public record PlayerDto(
    Long id,
    Long teamId,
    String teamName,
    String flagEmoji,
    String name,
    LocalDate dateOfBirth,
    String nationality,
    String position,
    Integer shirtNumber,
    String club,
    Integer caps,
    Integer goals,
    Integer heightCm,
    Long marketValueEur,
    Boolean isCaptain
) {}
