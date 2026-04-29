package com.worldcup.domain.comment;

import com.worldcup.config.AppProperties;
import com.worldcup.config.SecurityConfig;
import com.worldcup.domain.match.Match;
import com.worldcup.domain.match.MatchRepository;
import com.worldcup.domain.player.Player;
import com.worldcup.domain.player.PlayerRepository;
import com.worldcup.domain.team.Team;
import com.worldcup.domain.team.TeamRepository;
import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserRepository;
import com.worldcup.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CommentController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties(AppProperties.class)
@TestPropertySource(properties = {
    "DATABASE_URL=placeholder",
    "app.jwt.secret=dGVzdHNlY3JldGtleWZvcnVuaXR0ZXN0aW5ncHVycG9zZXM=",
    "app.jwt.access-expiry-ms=900000",
    "app.jwt.refresh-expiry-ms=604800000",
    "app.cors.allowed-origins=http://localhost:5173",
    "app.cookie.secure=false",
    "app.mail.from=noreply@test.local",
    "app.mail.frontend-url=http://localhost:5173"
})
class CommentControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean JwtService jwtService;
    @MockitoBean CommentRepository commentRepo;
    @MockitoBean CommentLikeRepository likeRepo;
    @MockitoBean UserRepository userRepo;
    @MockitoBean MatchRepository matchRepo;
    @MockitoBean TeamRepository teamRepo;
    @MockitoBean PlayerRepository playerRepo;
    @MockitoBean CommentMapper commentMapper;

    private static final String USER_TOKEN  = "test-user-token";
    private static final Long   USER_ID     = 1L;
    private static final Long   OTHER_ID    = 2L;
    private static final Long   COMMENT_ID  = 10L;
    private static final Long   MATCH_ID    = 100L;

    private final OffsetDateTime now = OffsetDateTime.now();

    private final CommentDto sampleDto = new CommentDto(
        COMMENT_ID, USER_ID, "Test User", null,
        MATCH_ID, null, null, null,
        "Hello world", false, 0, false,
        now, now
    );

    @BeforeEach
    void setupJwt() {
        lenient().when(jwtService.isValid(USER_TOKEN)).thenReturn(true);
        lenient().when(jwtService.userId(USER_TOKEN)).thenReturn(USER_ID);
        lenient().when(jwtService.role(USER_TOKEN)).thenReturn("USER");
    }

    // ---- GET /api/comments?matchId ----

    @Test
    void listByMatch_returnsComments() throws Exception {
        Comment mockComment = mock(Comment.class);
        when(mockComment.getId()).thenReturn(COMMENT_ID);
        when(commentRepo.findByMatchIdOrderByCreatedAtAsc(MATCH_ID)).thenReturn(List.of(mockComment));
        when(likeRepo.findLikedCommentIds(anyLong(), anyList())).thenReturn(Set.of());
        when(commentMapper.toDto(any(), anyBoolean())).thenReturn(sampleDto);

        mockMvc.perform(get("/api/comments").param("matchId", String.valueOf(MATCH_ID))
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].body").value("Hello world"));
    }

    @Test
    void listWithNoParams_returns400() throws Exception {
        mockMvc.perform(get("/api/comments"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void listUnauthenticated_returnsCommentsWithoutLikedByMe() throws Exception {
        Comment mockComment = mock(Comment.class);
        when(mockComment.getId()).thenReturn(COMMENT_ID);
        when(commentRepo.findByMatchIdOrderByCreatedAtAsc(MATCH_ID)).thenReturn(List.of(mockComment));
        when(commentMapper.toDto(any(), eq(false))).thenReturn(sampleDto);

        mockMvc.perform(get("/api/comments").param("matchId", String.valueOf(MATCH_ID)))
            .andExpect(status().isOk());

        // viewerId is null, so likedIds is empty Set — no repo call needed
        verify(likeRepo, never()).findLikedCommentIds(anyLong(), anyList());
    }

    // ---- POST /api/comments ----

    @Test
    void createComment_validMatchTarget_returns201() throws Exception {
        User mockUser = mock(User.class);
        Match mockMatch = mock(Match.class);
        Comment savedComment = mock(Comment.class);

        when(userRepo.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(matchRepo.findById(MATCH_ID)).thenReturn(Optional.of(mockMatch));
        when(commentRepo.save(any())).thenReturn(savedComment);
        when(commentMapper.toDto(any(), eq(false))).thenReturn(sampleDto);

        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"match_id": 100, "body": "Great match!"}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.body").value("Hello world"));
    }

    @Test
    void createComment_noTarget_returns400() throws Exception {
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"body": "Hello"}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_multipleTargets_returns400() throws Exception {
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"match_id": 1, "team_id": 2, "body": "Hello"}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_blankBody_returns400() throws Exception {
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"match_id": 100, "body": "  "}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"match_id": 100, "body": "Hello"}
                    """))
            .andExpect(status().isUnauthorized());
    }

    // ---- PUT /api/comments/{id} ----

    @Test
    void updateComment_ownComment_returns200() throws Exception {
        Comment mockComment = mock(Comment.class);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(USER_ID);
        when(mockComment.getUser()).thenReturn(mockUser);
        when(mockComment.getId()).thenReturn(COMMENT_ID);
        when(mockComment.getIsDeleted()).thenReturn(false);
        when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(mockComment));
        when(commentRepo.save(any())).thenReturn(mockComment);
        when(likeRepo.existsByUserIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(false);
        when(commentMapper.toDto(any(), anyBoolean())).thenReturn(sampleDto);

        mockMvc.perform(put("/api/comments/{id}", COMMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"body": "Updated text"}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isOk());
    }

    @Test
    void updateComment_otherUsersComment_returns403() throws Exception {
        Comment mockComment = mock(Comment.class);
        User otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn(OTHER_ID);
        when(mockComment.getUser()).thenReturn(otherUser);
        when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(mockComment));

        mockMvc.perform(put("/api/comments/{id}", COMMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"body": "Updated text"}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isForbidden());
    }

    // ---- DELETE /api/comments/{id} ----

    @Test
    void deleteComment_ownComment_returns204() throws Exception {
        Comment mockComment = mock(Comment.class);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(USER_ID);
        when(mockComment.getUser()).thenReturn(mockUser);
        when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(mockComment));

        mockMvc.perform(delete("/api/comments/{id}", COMMENT_ID)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isNoContent());

        verify(commentRepo).softDelete(COMMENT_ID, USER_ID);
    }

    @Test
    void deleteComment_otherUsersComment_returns403() throws Exception {
        Comment mockComment = mock(Comment.class);
        User otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn(OTHER_ID);
        when(mockComment.getUser()).thenReturn(otherUser);
        when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(mockComment));

        mockMvc.perform(delete("/api/comments/{id}", COMMENT_ID)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isForbidden());
    }

    // ---- POST /api/comments/{id}/likes ----

    @Test
    void likeComment_notYetLiked_incrementsAndReturns200() throws Exception {
        when(commentRepo.existsById(COMMENT_ID)).thenReturn(true);
        when(likeRepo.existsByUserIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(false);

        mockMvc.perform(post("/api/comments/{id}/likes", COMMENT_ID)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isOk());

        verify(commentRepo).incrementLikeCount(COMMENT_ID);
    }

    @Test
    void likeComment_alreadyLiked_idempotentNoDoubleIncrement() throws Exception {
        when(commentRepo.existsById(COMMENT_ID)).thenReturn(true);
        when(likeRepo.existsByUserIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(true);

        mockMvc.perform(post("/api/comments/{id}/likes", COMMENT_ID)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isOk());

        verify(commentRepo, never()).incrementLikeCount(anyLong());
    }

    @Test
    void likeComment_notFound_returns404() throws Exception {
        when(commentRepo.existsById(COMMENT_ID)).thenReturn(false);

        mockMvc.perform(post("/api/comments/{id}/likes", COMMENT_ID)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isNotFound());
    }

    // ---- DELETE /api/comments/{id}/likes ----

    @Test
    void unlikeComment_wasLiked_decrementsAndReturns204() throws Exception {
        when(likeRepo.existsByUserIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(true);

        mockMvc.perform(delete("/api/comments/{id}/likes", COMMENT_ID)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isNoContent());

        verify(commentRepo).decrementLikeCount(COMMENT_ID);
    }

    @Test
    void unlikeComment_notLiked_idempotentNoDecrement() throws Exception {
        when(likeRepo.existsByUserIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(false);

        mockMvc.perform(delete("/api/comments/{id}/likes", COMMENT_ID)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isNoContent());

        verify(commentRepo, never()).decrementLikeCount(anyLong());
    }
}
