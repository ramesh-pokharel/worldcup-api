package com.worldcup.domain.prediction;

import com.worldcup.domain.match.Match;
import com.worldcup.domain.match.MatchRepository;
import com.worldcup.domain.match.MatchStatus;
import com.worldcup.domain.tournament.TournamentPredictionRepository;
import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionRepository predRepo;
    private final PredictionLeaderboardRepository leaderboardRepo;
    private final TournamentPredictionRepository tpRepo;
    private final MatchRepository matchRepo;
    private final UserRepository userRepo;
    private final PredictionMapper predMapper;
    private final LeaderboardMapper leaderboardMapper;

    record PredictionRequest(
        @NotNull Long matchId,
        @NotNull @Min(0) Integer predictedTeam1Score,
        @NotNull @Min(0) Integer predictedTeam2Score
    ) {}

    record UpdateRequest(
        @NotNull @Min(0) Integer predictedTeam1Score,
        @NotNull @Min(0) Integer predictedTeam2Score
    ) {}

    // ---- Submit prediction ----

    @Transactional
    @PostMapping("/api/predictions")
    public ResponseEntity<PredictionDto> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PredictionRequest req) {

        Match match = matchRepo.findById(req.matchId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
        if (match.getStatus() != MatchStatus.SCHEDULED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Predictions are closed for this match");
        if (predRepo.existsByUserIdAndMatchId(userId, req.matchId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already submitted a prediction for this match");

        User user = userRepo.findById(userId).orElseThrow();
        Prediction saved = predRepo.save(Prediction.builder()
                .user(user).match(match)
                .predictedTeam1Score(req.predictedTeam1Score())
                .predictedTeam2Score(req.predictedTeam2Score())
                .pointsEarned(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(predMapper.toDto(saved));
    }

    // ---- Update prediction (only while match is still SCHEDULED) ----

    @Transactional
    @PutMapping("/api/predictions/{id}")
    public PredictionDto update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequest req) {

        Prediction pred = predRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!pred.getUser().getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (pred.getMatch().getStatus() != MatchStatus.SCHEDULED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot edit — match has already started");

        pred.setPredictedTeam1Score(req.predictedTeam1Score());
        pred.setPredictedTeam2Score(req.predictedTeam2Score());
        pred.setUpdatedAt(OffsetDateTime.now());
        return predMapper.toDto(predRepo.save(pred));
    }

    // ---- Get predictions for a match (hidden until match starts) ----

    @GetMapping("/api/predictions")
    public List<PredictionDto> listByMatch(
            @RequestParam Long matchId,
            @AuthenticationPrincipal Long userId) {

        Match match = matchRepo.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));

        // Only show all predictions once the match is no longer SCHEDULED
        if (match.getStatus() == MatchStatus.SCHEDULED) {
            if (userId == null) return List.of();
            return predRepo.findByUserIdAndMatchId(userId, matchId)
                    .map(p -> List.of(predMapper.toDto(p)))
                    .orElse(List.of());
        }
        return predMapper.toDtoList(predRepo.findByMatchId(matchId));
    }

    // ---- My predictions ----

    @GetMapping("/api/predictions/me")
    public List<PredictionDto> myPredictions(@AuthenticationPrincipal Long userId) {
        return predMapper.toDtoList(predRepo.findByUserId(userId));
    }

    // ---- Leaderboard ----

    @GetMapping("/api/leaderboard")
    public List<LeaderboardDto> leaderboard() {
        return leaderboardMapper.toDtoList(leaderboardRepo.findTop5ByOrderByTotalPointsDescExactScoresDesc());
    }

    // ---- Admin: recalculate scores for all finished matches ----

    /*
     * WHY @PreAuthorize here: this endpoint is destructive (rewrites all points).
     * We add a method-level guard in addition to the SecurityConfig rule so the
     * intent is explicit in the code, not just hidden in config.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/leaderboard/recalculate")
    public ResponseEntity<Void> recalculate() {
        List<Prediction> predictions = predRepo.findAll();

        for (Prediction p : predictions) {
            Match m = p.getMatch();
            if (m.getStatus() != MatchStatus.FINISHED
                    || m.getTeam1Score() == null || m.getTeam2Score() == null) continue;

            int points;
            if (p.getPredictedTeam1Score().equals(m.getTeam1Score())
                    && p.getPredictedTeam2Score().equals(m.getTeam2Score())) {
                points = 3;
            } else if (Integer.signum(p.getPredictedTeam1Score() - p.getPredictedTeam2Score())
                    == Integer.signum(m.getTeam1Score() - m.getTeam2Score())) {
                points = 1;
            } else {
                points = 0;
            }
            p.setPointsEarned(points);
        }
        predRepo.saveAll(predictions);

        // Rebuild leaderboard from scratch (match predictions only first)
        leaderboardRepo.deleteAll();
        predRepo.findAll().stream()
                .filter(p -> p.getMatch().getStatus() == MatchStatus.FINISHED)
                .collect(java.util.stream.Collectors.groupingBy(p -> p.getUser().getId()))
                .forEach((uid, preds) -> {
                    // Load a fresh User reference — using the lazy proxy from Prediction causes
                    // Hibernate 7 AssertionFailure: null identifier on @MapsId merge()
                    User user = userRepo.findById(uid).orElseThrow();
                    int total   = preds.stream().mapToInt(p -> p.getPointsEarned() != null ? p.getPointsEarned() : 0).sum();
                    int exact   = (int) preds.stream().filter(p -> p.getPointsEarned() != null && p.getPointsEarned() == 3).count();
                    int correct = (int) preds.stream().filter(p -> p.getPointsEarned() != null && p.getPointsEarned() == 1).count();
                    // Omit userId so isNew()=true → persist() instead of merge(), letting @MapsId derive the PK
                    leaderboardRepo.save(PredictionLeaderboard.builder()
                            .user(user)
                            .totalPoints(total).exactScores(exact).correctResults(correct)
                            .tournamentPoints(0)
                            .updatedAt(OffsetDateTime.now())
                            .build());
                });

        // Second pass: add tournament prediction points
        tpRepo.findByPointsEarnedIsNotNull().forEach(tp -> {
            int pts = tp.getPointsEarned();
            Long uid = tp.getUser().getId();
            leaderboardRepo.findById(uid).ifPresentOrElse(
                lb -> {
                    lb.setTournamentPoints(pts);
                    lb.setTotalPoints(lb.getTotalPoints() + pts);
                    leaderboardRepo.save(lb);
                },
                () -> {
                    User user = userRepo.findById(uid).orElseThrow();
                    leaderboardRepo.save(PredictionLeaderboard.builder()
                            .user(user)
                            .totalPoints(pts).exactScores(0).correctResults(0)
                            .tournamentPoints(pts)
                            .updatedAt(OffsetDateTime.now())
                            .build());
                }
            );
        });

        return ResponseEntity.ok().build();
    }
}
