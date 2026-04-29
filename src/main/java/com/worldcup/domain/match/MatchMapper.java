package com.worldcup.domain.match;

import com.worldcup.domain.team.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MatchMapper {

    @Mapping(source = "team1.id",                target = "team1Id")
    @Mapping(source = "team1.country.name",      target = "team1Name")
    @Mapping(source = "team1.country.flagEmoji",  target = "team1Flag")
    @Mapping(expression = "java(flagCode(match.getTeam1()))", target = "team1FlagCode")
    @Mapping(source = "team1.kitPrimary",         target = "team1Kit")
    @Mapping(source = "team2.id",                target = "team2Id")
    @Mapping(source = "team2.country.name",      target = "team2Name")
    @Mapping(source = "team2.country.flagEmoji",  target = "team2Flag")
    @Mapping(expression = "java(flagCode(match.getTeam2()))", target = "team2FlagCode")
    @Mapping(source = "team2.kitPrimary",         target = "team2Kit")
    @Mapping(source = "stadium.name",            target = "stadiumName")
    @Mapping(source = "stadium.city",            target = "stadiumCity")
    // MapStruct calls .name() on the enum automatically → String
    @Mapping(source = "stage",                   target = "stage")
    @Mapping(source = "status",                  target = "status")
    MatchDto toDto(Match match);

    List<MatchDto> toDtoList(List<Match> matches);

    default String flagCode(Team team) {
        if (team == null || team.getCountry() == null) return null;
        return "ENG".equals(team.getCountry().getCodeIso3())
                ? "gb-eng"
                : team.getCountry().getCodeIso2().toLowerCase();
    }
}
