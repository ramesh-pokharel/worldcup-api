package com.worldcup.domain.player;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    @Mapping(source = "team.id",               target = "teamId")
    @Mapping(source = "team.country.name",     target = "teamName")
    @Mapping(source = "team.country.flagEmoji", target = "flagEmoji")
    PlayerDto toDto(Player player);

    List<PlayerDto> toDtoList(List<Player> players);
}
