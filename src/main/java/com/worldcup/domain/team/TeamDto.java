package com.worldcup.domain.team;

public record TeamDto(
    Long id,
    String countryName,
    String codeIso2,
    String codeIso3,
    String flagEmoji,
    String flagCode,
    String confederation,
    Integer fifaRanking,
    String groupId,
    String manager,
    String kitPrimary,
    String kitSecondary,
    Boolean isHost
) {}
