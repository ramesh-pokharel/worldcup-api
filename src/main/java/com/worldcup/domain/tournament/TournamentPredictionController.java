package com.worldcup.domain.tournament;

import com.worldcup.domain.player.Player;
import com.worldcup.domain.player.PlayerRepository;
import com.worldcup.domain.team.Team;
import com.worldcup.domain.team.TeamRepository;
import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/tournament-predictions")
@RequiredArgsConstructor
public class TournamentPredictionController {

    private final TournamentPredictionRepository tpRepo;
    private final UserRepository userRepo;
    private final TeamRepository teamRepo;
    private final PlayerRepository playerRepo;
    private final TournamentPredictionMapper tpMapper;

    record TournamentRequest(Long predictedWinnerId, Long predictedRunnerUpId, Long predictedTopScorerId) {}

    @GetMapping("/me")
    public ResponseEntity<TournamentPredictionDto> getMyPrediction(
            @AuthenticationPrincipal Long userId) {
        return tpRepo.findByUserId(userId)
                .map(tpMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @PostMapping
    public ResponseEntity<TournamentPredictionDto> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody TournamentRequest req) {

        if (tpRepo.findByUserId(userId).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You already have a tournament prediction — use PUT to update it");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tpMapper.toDto(tpRepo.save(buildEntity(userId, req, null))));
    }

    @Transactional
    @PutMapping
    public TournamentPredictionDto update(
            @AuthenticationPrincipal Long userId,
            @RequestBody TournamentRequest req) {

        TournamentPrediction existing = tpRepo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No tournament prediction found — use POST to create one"));
        return tpMapper.toDto(tpRepo.save(buildEntity(userId, req, existing.getId())));
    }

    private TournamentPrediction buildEntity(Long userId, TournamentRequest req, Long existingId) {
        User user = userRepo.findById(userId).orElseThrow();

        Team winner   = req.predictedWinnerId()   != null ? teamRepo.findById(req.predictedWinnerId()).orElseThrow()   : null;
        Team runnerUp = req.predictedRunnerUpId()  != null ? teamRepo.findById(req.predictedRunnerUpId()).orElseThrow() : null;
        Player scorer = req.predictedTopScorerId() != null ? playerRepo.findById(req.predictedTopScorerId()).orElseThrow() : null;

        return TournamentPrediction.builder()
                .id(existingId)
                .user(user)
                .predictedWinner(winner)
                .predictedRunnerUp(runnerUp)
                .predictedTopScorer(scorer)
                .createdAt(existingId == null ? OffsetDateTime.now() : null)
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}
