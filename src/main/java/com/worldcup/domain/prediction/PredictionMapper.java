package com.worldcup.domain.prediction;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PredictionMapper {

    @Mapping(source = "user.id",          target = "userId")
    @Mapping(source = "user.displayName", target = "authorName")
    @Mapping(source = "match.id",         target = "matchId")
    PredictionDto toDto(Prediction prediction);

    List<PredictionDto> toDtoList(List<Prediction> predictions);
}
