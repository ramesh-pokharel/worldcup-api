package com.worldcup.domain.prediction;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LeaderboardMapper {

    @Mapping(source = "user.id",          target = "userId")
    @Mapping(source = "user.displayName", target = "displayName")
    @Mapping(source = "user.avatarUrl",   target = "avatarUrl")
    LeaderboardDto toDto(PredictionLeaderboard entry);

    List<LeaderboardDto> toDtoList(List<PredictionLeaderboard> entries);
}
