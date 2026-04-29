package com.worldcup.domain.comment;

import com.worldcup.AbstractRepositoryTest;
import com.worldcup.TestDataFactory;
import com.worldcup.domain.match.Match;
import com.worldcup.domain.match.MatchRepository;
import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CommentRepositoryTest extends AbstractRepositoryTest {

    @Autowired CommentRepository commentRepo;
    @Autowired CommentLikeRepository likeRepo;
    @Autowired UserRepository userRepo;
    @Autowired MatchRepository matchRepo;
    @PersistenceContext EntityManager em;

    private User user;
    private Match match;

    @BeforeEach
    void setUp() {
        user  = userRepo.save(TestDataFactory.user("commenter").build());
        match = matchRepo.findAllByOrderByScheduledAtAsc().get(0);
    }

    private Comment savedComment(String body) {
        return commentRepo.save(Comment.builder()
                .user(user).match(match)
                .body(body).isDeleted(false).likeCount(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
    }

    // ---- findByMatchId ----

    @Test
    void findByMatchId_returnsCommentsForThatMatch_orderedByCreatedAt() {
        Comment c1 = savedComment("first");
        Comment c2 = savedComment("second");
        em.flush(); em.clear();

        List<Comment> comments = commentRepo.findByMatchIdOrderByCreatedAtAsc(match.getId());
        List<Long> ids = comments.stream().map(Comment::getId).toList();

        assertThat(ids).contains(c1.getId(), c2.getId());
        // Chronological order
        int idx1 = ids.indexOf(c1.getId());
        int idx2 = ids.indexOf(c2.getId());
        assertThat(idx1).isLessThan(idx2);
    }

    @Test
    void findByMatchId_eagerlyLoadsUser_noLazyException() {
        savedComment("lazy-check");
        em.flush(); em.clear();

        List<Comment> comments = commentRepo.findByMatchIdOrderByCreatedAtAsc(match.getId());
        assertThat(comments).isNotEmpty();
        // If @EntityGraph({"user"}) is missing, this line throws LazyInitializationException
        assertThat(comments.get(0).getUser().getDisplayName()).isNotBlank();
    }

    @Test
    void findByMatchId_unknownMatch_returnsEmpty() {
        assertThat(commentRepo.findByMatchIdOrderByCreatedAtAsc(Long.MAX_VALUE)).isEmpty();
    }

    // ---- softDelete ----

    @Test
    void softDelete_setsIsDeletedTrue_bodyUnchanged() {
        Comment c = savedComment("to be deleted");
        em.flush(); em.clear();

        commentRepo.softDelete(c.getId(), user.getId());
        em.flush(); em.clear();

        Comment deleted = commentRepo.findById(c.getId()).orElseThrow();
        assertThat(deleted.getIsDeleted()).isTrue();
        assertThat(deleted.getBody()).isEqualTo("to be deleted");
    }

    @Test
    void softDelete_wrongUser_doesNotDelete() {
        Comment c = savedComment("someone else's comment");
        User other = userRepo.save(TestDataFactory.user("other").build());
        em.flush(); em.clear();

        commentRepo.softDelete(c.getId(), other.getId());
        em.flush(); em.clear();

        assertThat(commentRepo.findById(c.getId()).orElseThrow().getIsDeleted()).isFalse();
    }

    // ---- like count ----

    @Test
    void incrementLikeCount_increasesCountByOne() {
        Comment c = savedComment("likeable");
        assertThat(c.getLikeCount()).isZero();
        em.flush(); em.clear();

        commentRepo.incrementLikeCount(c.getId());
        em.flush(); em.clear();

        assertThat(commentRepo.findById(c.getId()).orElseThrow().getLikeCount()).isEqualTo(1);
    }

    @Test
    void decrementLikeCount_doesNotGoBelowZero() {
        Comment c = savedComment("already at zero");
        em.flush(); em.clear();

        commentRepo.decrementLikeCount(c.getId());
        em.flush(); em.clear();

        assertThat(commentRepo.findById(c.getId()).orElseThrow().getLikeCount()).isZero();
    }

    // ---- CommentLikeRepository ----

    @Test
    void existsByUserIdAndCommentId_trueAfterSave_falseBeforeSave() {
        Comment c = savedComment("for likes");
        em.flush(); em.clear();

        assertThat(likeRepo.existsByUserIdAndCommentId(user.getId(), c.getId())).isFalse();
        likeRepo.save(new CommentLike(user.getId(), c.getId()));
        em.flush(); em.clear();
        assertThat(likeRepo.existsByUserIdAndCommentId(user.getId(), c.getId())).isTrue();
    }

    @Test
    void findLikedCommentIds_returnsOnlyLikedByThatUser() {
        Comment liked   = savedComment("liked");
        Comment unliked = savedComment("not liked");
        likeRepo.save(new CommentLike(user.getId(), liked.getId()));
        em.flush(); em.clear();

        Set<Long> likedIds = likeRepo.findLikedCommentIds(
                user.getId(), List.of(liked.getId(), unliked.getId()));

        assertThat(likedIds).containsExactly(liked.getId());
    }
}
