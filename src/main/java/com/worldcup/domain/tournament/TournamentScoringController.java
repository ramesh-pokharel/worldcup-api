package com.worldcup.domain.tournament;

import com.worldcup.domain.player.Player;
import com.worldcup.domain.player.PlayerRepository;
import com.worldcup.domain.team.Team;
import com.worldcup.domain.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class TournamentScoringController {

    private final TournamentResultRepository trRepo;
    private final TournamentPredictionRepository tpRepo;
    private final TeamRepository teamRepo;
    private final PlayerRepository playerRepo;

    record TournamentResultRequest(Long actualWinnerId, Long actualTopScorerId) {}

    @Transactional
    @PostMapping("/api/admin/tournament-result")
    public ResponseEntity<Void> setResult(@RequestBody TournamentResultRequest req) {
        Team winner = req.actualWinnerId() != null
                ? teamRepo.findById(req.actualWinnerId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"))
                : null;
        Player scorer = req.actualTopScorerId() != null
                ? playerRepo.findById(req.actualTopScorerId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"))
                : null;
        trRepo.save(TournamentResult.builder()
                .actualWinner(winner)
                .actualTopScorer(scorer)
                .setAt(OffsetDateTime.now())
                .build());
        return ResponseEntity.ok().build();
    }

    @Transactional
    @PostMapping("/api/admin/leaderboard/score-tournament")
    public ResponseEntity<Map<String, Integer>> scoreTournament() {
        TournamentResult result = trRepo.findTopByOrderBySetAtDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No tournament result set — call POST /api/admin/tournament-result first"));

        Long actualWinnerId  = result.getActualWinner()    != null ? result.getActualWinner().getId()    : null;
        Long actualScorerId  = result.getActualTopScorer() != null ? result.getActualTopScorer().getId() : null;

        List<TournamentPrediction> predictions = tpRepo.findAll();
        int scored = 0;
        for (TournamentPrediction tp : predictions) {
            int pts = 0;
            if (actualWinnerId != null && tp.getPredictedWinner() != null
                    && actualWinnerId.equals(tp.getPredictedWinner().getId())) {
                pts += 2;
            }
            if (actualScorerId != null && tp.getPredictedTopScorer() != null
                    && actualScorerId.equals(tp.getPredictedTopScorer().getId())) {
                pts += 1;
            }
            tp.setPointsEarned((short) pts);
            scored++;
        }
        tpRepo.saveAll(predictions);
        return ResponseEntity.ok(Map.of("scored", scored));
    }
}
