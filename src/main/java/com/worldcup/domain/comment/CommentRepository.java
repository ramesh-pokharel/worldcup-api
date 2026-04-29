package com.worldcup.domain.comment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByMatchIdOrderByCreatedAtAsc(Long matchId);

    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByTeamIdOrderByCreatedAtAsc(Long teamId);

    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByPlayerIdOrderByCreatedAtAsc(Long playerId);

    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :id AND c.user.id = :userId")
    void softDelete(Long id, Long userId);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void incrementLikeCount(Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = GREATEST(0, c.likeCount - 1) WHERE c.id = :id")
    void decrementLikeCount(Long id);
}
