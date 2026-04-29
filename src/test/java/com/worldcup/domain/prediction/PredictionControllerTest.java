package com.worldcup.domain.prediction;

import com.worldcup.config.AppProperties;
import com.worldcup.config.SecurityConfig;
import com.worldcup.domain.match.Match;
import com.worldcup.domain.match.MatchRepository;
import com.worldcup.domain.match.MatchStatus;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PredictionController.class)
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
class PredictionControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean JwtService jwtService;
    @MockitoBean PredictionRepository predRepo;
    @MockitoBean PredictionLeaderboardRepository leaderboardRepo;
    @MockitoBean MatchRepository matchRepo;
    @MockitoBean UserRepository userRepo;
    @MockitoBean PredictionMapper predMapper;
    @MockitoBean LeaderboardMapper leaderboardMapper;

    private static final String USER_TOKEN  = "test-user-token";
    private static final String ADMIN_TOKEN = "test-admin-token";
    private static final Long   USER_ID     = 1L;
    private static final Long   ADMIN_ID    = 99L;
    private static final Long   OTHER_ID    = 2L;
    private static final Long   MATCH_ID    = 100L;
    private static final Long   PRED_ID     = 10L;

    private final OffsetDateTime now = OffsetDateTime.now();

    private final PredictionDto samplePredDto = new PredictionDto(
        PRED_ID, USER_ID, "Test User", MATCH_ID, 2, 1, null, now, now
    );

    private final LeaderboardDto sampleLeaderDto =
        new LeaderboardDto(USER_ID, "Test User", null, 9, 3, 0);

    @BeforeEach
    void setupJwt() {
        lenient().when(jwtService.isValid(USER_TOKEN)).thenReturn(true);
        lenient().when(jwtService.userId(USER_TOKEN)).thenReturn(USER_ID);
        lenient().when(jwtService.role(USER_TOKEN)).thenReturn("USER");

        lenient().when(jwtService.isValid(ADMIN_TOKEN)).thenReturn(true);
        lenient().when(jwtService.userId(ADMIN_TOKEN)).thenReturn(ADMIN_ID);
        lenient().when(jwtService.role(ADMIN_TOKEN)).thenReturn("ADMIN");
    }

    // ---- POST /api/predictions ----

    @Test
    void createPrediction_scheduledMatch_returns201() throws Exception {
        Match match = mock(Match.class);
        User user   = mock(User.class);
        Prediction saved = mock(Prediction.class);

        when(matchRepo.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(match.getStatus()).thenReturn(MatchStatus.SCHEDULED);
        when(predRepo.existsByUserIdAndMatchId(USER_ID, MATCH_ID)).thenReturn(false);
        when(userRepo.findById(USER_ID)).thenReturn(Optional.of(user));
        when(predRepo.save(any())).thenReturn(saved);
        when(predMapper.toDto(saved)).thenReturn(samplePredDto);

        mockMvc.perform(post("/api/predictions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"match_id":100,"predicted_team1_score":2,"predicted_team2_score":1}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.predicted_team1_score").value(2));
    }

    @Test
    void createPrediction_matchNotScheduled_returns409() throws Exception {
        Match match = mock(Match.class);
        when(matchRepo.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(match.getStatus()).thenReturn(MatchStatus.LIVE);

        mockMvc.perform(post("/api/predictions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"match_id":100,"predicted_team1_score":2,"predicted_team2_score":1}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isConflict());
    }

    @Test
    void createPrediction_duplicatePrediction_returns409() throws Exception {
        Match match = mock(Match.class);
        when(matchRepo.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(match.getStatus()).thenReturn(MatchStatus.SCHEDULED);
        when(predRepo.existsByUserIdAndMatchId(USER_ID, MATCH_ID)).thenReturn(true);

        mockMvc.perform(post("/api/predictions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"match_id":100,"predicted_team1_score":2,"predicted_team2_score":1}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isConflict());
    }

    @Test
    void createPrediction_negativeScore_returns400() throws Exception {
        mockMvc.perform(post("/api/predictions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"match_id":100,"predicted_team1_score":-1,"predicted_team2_score":0}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createPrediction_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/predictions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"match_id":100,"predicted_team1_score":2,"predicted_team2_score":1}
                    """))
            .andExpect(status().isUnauthorized());
    }

    // ---- PUT /api/predictions/{id} ----

    @Test
    void updatePrediction_ownScheduledMatch_returns200() throws Exception {
        Prediction pred = mock(Prediction.class);
        User owner      = mock(User.class);
        Match match     = mock(Match.class);

        when(predRepo.findById(PRED_ID)).thenReturn(Optional.of(pred));
        when(owner.getId()).thenReturn(USER_ID);
        when(pred.getUser()).thenReturn(owner);
        when(pred.getMatch()).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.SCHEDULED);
        when(predRepo.save(pred)).thenReturn(pred);
        when(predMapper.toDto(pred)).thenReturn(samplePredDto);

        mockMvc.perform(put("/api/predictions/{id}", PRED_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"predicted_team1_score":3,"predicted_team2_score":0}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isOk());
    }

    @Test
    void updatePrediction_notOwner_returns403() throws Exception {
        Prediction pred = mock(Prediction.class);
        User other      = mock(User.class);

        when(predRepo.findById(PRED_ID)).thenReturn(Optional.of(pred));
        when(other.getId()).thenReturn(OTHER_ID);
        when(pred.getUser()).thenReturn(other);

        mockMvc.perform(put("/api/predictions/{id}", PRED_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"predicted_team1_score":3,"predicted_team2_score":0}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isForbidden());
    }

    @Test
    void updatePrediction_matchAlreadyStarted_returns409() throws Exception {
        Prediction pred = mock(Prediction.class);
        User owner      = mock(User.class);
        Match match     = mock(Match.class);

        when(predRepo.findById(PRED_ID)).thenReturn(Optional.of(pred));
        when(owner.getId()).thenReturn(USER_ID);
        when(pred.getUser()).thenReturn(owner);
        when(pred.getMatch()).thenReturn(match);
        when(match.getStatus()).thenReturn(MatchStatus.LIVE);

        mockMvc.perform(put("/api/predictions/{id}", PRED_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"predicted_team1_score":3,"predicted_team2_score":0}
                    """)
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isConflict());
    }

    // ---- GET /api/predictions?matchId ----

    @Test
    void listByMatch_scheduledAndUnauthenticated_returnsEmptyList() throws Exception {
        Match match = mock(Match.class);
        when(matchRepo.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(match.getStatus()).thenReturn(MatchStatus.SCHEDULED);

        mockMvc.perform(get("/api/predictions").param("matchId", String.valueOf(MATCH_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void listByMatch_scheduledAndAuthenticated_returnsOnlyOwnPrediction() throws Exception {
        Match match = mock(Match.class);
        Prediction ownPred = mock(Prediction.class);

        when(matchRepo.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(match.getStatus()).thenReturn(MatchStatus.SCHEDULED);
        when(predRepo.findByUserIdAndMatchId(USER_ID, MATCH_ID)).thenReturn(Optional.of(ownPred));
        when(predMapper.toDto(ownPred)).thenReturn(samplePredDto);

        mockMvc.perform(get("/api/predictions").param("matchId", String.valueOf(MATCH_ID))
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listByMatch_finishedMatch_returnsAllPredictions() throws Exception {
        Match match   = mock(Match.class);
        Prediction p1 = mock(Prediction.class);
        Prediction p2 = mock(Prediction.class);

        PredictionDto dto2 = new PredictionDto(11L, OTHER_ID, "Other", MATCH_ID, 1, 0, 1, now, now);

        when(matchRepo.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(match.getStatus()).thenReturn(MatchStatus.FINISHED);
        when(predRepo.findByMatchId(MATCH_ID)).thenReturn(List.of(p1, p2));
        when(predMapper.toDtoList(List.of(p1, p2))).thenReturn(List.of(samplePredDto, dto2));

        mockMvc.perform(get("/api/predictions").param("matchId", String.valueOf(MATCH_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    // ---- GET /api/predictions/me ----

    @Test
    void myPredictions_authenticated_returnsList() throws Exception {
        when(predRepo.findByUserId(USER_ID)).thenReturn(List.of());
        when(predMapper.toDtoList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/predictions/me")
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    // ---- GET /api/leaderboard ----

    @Test
    void leaderboard_publicEndpoint_returnsTop50() throws Exception {
        when(leaderboardRepo.findTop50ByOrderByTotalPointsDescExactScoresDesc())
            .thenReturn(List.of());
        when(leaderboardMapper.toDtoList(List.of())).thenReturn(List.of(sampleLeaderDto));

        mockMvc.perform(get("/api/leaderboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].total_points").value(9));
    }

    // ---- POST /api/admin/leaderboard/recalculate ----

    @Test
    void recalculate_asAdmin_returns200() throws Exception {
        when(predRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/api/admin/leaderboard/recalculate")
                .cookie(new Cookie("access_token", ADMIN_TOKEN)))
            .andExpect(status().isOk());
    }

    @Test
    void recalculate_asUser_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/leaderboard/recalculate")
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isForbidden());
    }
}
