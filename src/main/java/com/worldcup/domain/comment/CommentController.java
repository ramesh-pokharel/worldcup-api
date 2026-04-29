package com.worldcup.domain.comment;

import com.worldcup.domain.match.Match;
import com.worldcup.domain.match.MatchRepository;
import com.worldcup.domain.player.Player;
import com.worldcup.domain.player.PlayerRepository;
import com.worldcup.domain.team.Team;
import com.worldcup.domain.team.TeamRepository;
import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepo;
    private final CommentLikeRepository likeRepo;
    private final UserRepository userRepo;
    private final MatchRepository matchRepo;
    private final TeamRepository teamRepo;
    private final PlayerRepository playerRepo;
    private final CommentMapper commentMapper;

    record CommentRequest(
        Long matchId,
        Long teamId,
        Long playerId,
        Long parentId,
        @NotBlank @Size(max = 2000) String body
    ) {}

    record UpdateRequest(@NotBlank @Size(max = 2000) String body) {}

    // ---- List ----

    @GetMapping
    public List<CommentDto> list(
            @RequestParam(required = false) Long matchId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long playerId,
            @AuthenticationPrincipal Long viewerId) {

        List<Comment> comments;
        if (matchId  != null) comments = commentRepo.findByMatchIdOrderByCreatedAtAsc(matchId);
        else if (teamId   != null) comments = commentRepo.findByTeamIdOrderByCreatedAtAsc(teamId);
        else if (playerId != null) comments = commentRepo.findByPlayerIdOrderByCreatedAtAsc(playerId);
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide matchId, teamId, or playerId");

        Set<Long> likedIds = viewerId != null
                ? likeRepo.findLikedCommentIds(viewerId, comments.stream().map(Comment::getId).toList())
                : Set.of();

        return comments.stream()
                .map(c -> commentMapper.toDto(c, likedIds.contains(c.getId())))
                .toList();
    }

    // ---- Create ----

    @Transactional
    @PostMapping
    public ResponseEntity<CommentDto> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CommentRequest req) {

        int targets = (req.matchId() != null ? 1 : 0)
                    + (req.teamId()  != null ? 1 : 0)
                    + (req.playerId() != null ? 1 : 0);
        if (targets != 1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide exactly one of matchId, teamId, playerId");

        User user = userRepo.findById(userId).orElseThrow();

        Match match   = req.matchId()  != null ? matchRepo.findById(req.matchId()).orElseThrow()   : null;
        Team team     = req.teamId()   != null ? teamRepo.findById(req.teamId()).orElseThrow()     : null;
        Player player = req.playerId() != null ? playerRepo.findById(req.playerId()).orElseThrow() : null;
        Comment parent = req.parentId() != null ? commentRepo.findById(req.parentId()).orElse(null) : null;

        Comment saved = commentRepo.save(Comment.builder()
                .user(user)
                .match(match).team(team).player(player)
                .parent(parent)
                .body(req.body())
                .isDeleted(false)
                .likeCount(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentMapper.toDto(saved, false));
    }

    // ---- Edit ----

    @Transactional
    @PutMapping("/{id}")
    public CommentDto update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequest req) {

        Comment comment = commentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!comment.getUser().getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (Boolean.TRUE.equals(comment.getIsDeleted()))
            throw new ResponseStatusException(HttpStatus.GONE, "Comment is deleted");

        comment.setBody(req.body());
        comment.setUpdatedAt(OffsetDateTime.now());
        commentRepo.save(comment);
        return commentMapper.toDto(comment, likeRepo.existsByUserIdAndCommentId(userId, id));
    }

    // ---- Delete (soft) ----

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        Comment comment = commentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!comment.getUser().getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        commentRepo.softDelete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ---- Likes ----

    @Transactional
    @PostMapping("/{id}/likes")
    public ResponseEntity<Void> like(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        if (!commentRepo.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!likeRepo.existsByUserIdAndCommentId(userId, id)) {
            likeRepo.save(new CommentLike(userId, id));
            commentRepo.incrementLikeCount(id);
        }
        return ResponseEntity.ok().build();
    }

    @Transactional
    @DeleteMapping("/{id}/likes")
    public ResponseEntity<Void> unlike(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        if (likeRepo.existsByUserIdAndCommentId(userId, id)) {
            likeRepo.deleteByUserIdAndCommentId(userId, id);
            commentRepo.decrementLikeCount(id);
        }
        return ResponseEntity.noContent().build();
    }
}
