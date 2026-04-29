package com.worldcup.domain.comment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "user.id",          target = "userId")
    @Mapping(source = "user.displayName", target = "authorName")
    @Mapping(source = "user.avatarUrl",   target = "authorAvatar")
    @Mapping(source = "match.id",         target = "matchId")
    @Mapping(source = "team.id",          target = "teamId")
    @Mapping(source = "player.id",        target = "playerId")
    @Mapping(source = "parent.id",        target = "parentId")
    @Mapping(source = "isDeleted",        target = "deleted")
    /*
     * WHY expression: the body must be masked as "[deleted]" when soft-deleted.
     * MapStruct can't express conditional logic via @Mapping alone, so we use
     * a Java expression that runs inline in the generated mapper.
     */
    @Mapping(target = "body",
             expression = "java(Boolean.TRUE.equals(comment.getIsDeleted()) ? \"[deleted]\" : comment.getBody())")
    @Mapping(target = "likedByMe", ignore = true)   // set by controller after a separate likes query
    CommentDto toDtoBase(Comment comment);

    /*
     * Default method so callers can pass likedByMe in one call.
     * Records are immutable, so we reconstruct with all fields from the base mapping.
     */
    default CommentDto toDto(Comment comment, boolean likedByMe) {
        CommentDto base = toDtoBase(comment);
        return new CommentDto(
            base.id(), base.userId(), base.authorName(), base.authorAvatar(),
            base.matchId(), base.teamId(), base.playerId(), base.parentId(),
            base.body(), base.deleted(), base.likeCount(),
            likedByMe,
            base.createdAt(), base.updatedAt()
        );
    }

    List<CommentDto> toDtoList(List<Comment> comments);
}
