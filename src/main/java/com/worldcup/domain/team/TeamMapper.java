package com.worldcup.domain.team;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    @Mapping(source = "country.name",                  target = "countryName")
    @Mapping(source = "country.codeIso2",              target = "codeIso2")
    @Mapping(source = "country.codeIso3",              target = "codeIso3")
    @Mapping(source = "country.flagEmoji",             target = "flagEmoji")
    @Mapping(source = "country.confederation.name",    target = "confederation")
    @Mapping(expression = "java(\"ENG\".equals(team.getCountry().getCodeIso3()) ? \"gb-eng\" : team.getCountry().getCodeIso2().toLowerCase())", target = "flagCode")
    TeamDto toDto(Team team);

    List<TeamDto> toDtoList(List<Team> teams);
}
