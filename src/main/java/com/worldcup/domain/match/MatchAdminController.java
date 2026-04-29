package com.worldcup.domain.match;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/matches")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class MatchAdminController {

    private final MatchRepository matchRepo;
    private final MatchMapper matchMapper;

    record MatchResultRequest(
        @NotNull MatchStatus status,

        @Min(0) Integer team1Score,
        @Min(0) Integer team2Score,

        // Only set when the match ends level and goes to a penalty shootout
        @Min(0) Integer team1ScorePen,
        @Min(0) Integer team2ScorePen
    ) {}

    @Transactional
    @PatchMapping("/{id}")
    public ResponseEntity<MatchDto> updateResult(
            @PathVariable Long id,
            @Valid @RequestBody MatchResultRequest req) {

        if (!matchRepo.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found");

        // Scores are mandatory when marking a match as FINISHED
        if (req.status() == MatchStatus.FINISHED
                && (req.team1Score() == null || req.team2Score() == null))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "team1Score and team2Score are required when status is FINISHED");

        // Penalty scores only make sense when the match ends level
        boolean scoresProvided = req.team1Score() != null && req.team2Score() != null;
        boolean isPenaltyResult = req.team1ScorePen() != null || req.team2ScorePen() != null;
        if (isPenaltyResult && scoresProvided && !req.team1Score().equals(req.team2Score()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Penalty scores can only be set when scores are level after 90 minutes");

        matchRepo.updateResult(
            id,
            req.status().name(),
            req.team1Score(),
            req.team2Score(),
            req.team1ScorePen(),
            req.team2ScorePen()
        );

        // Re-fetch with full associations so the response DTO is complete
        Match updated = matchRepo.findById(id).orElseThrow();
        return ResponseEntity.ok(matchMapper.toDto(updated));
    }
}
