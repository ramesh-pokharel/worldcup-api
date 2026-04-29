package com.worldcup.domain.community;

import com.worldcup.domain.player.PlayerRepository;
import com.worldcup.domain.prediction.PredictionRepository;
import com.worldcup.domain.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final TeamRepository       teamRepo;
    private final PlayerRepository     playerRepo;
    private final PredictionRepository predictionRepo;

    @GetMapping("/stats")
    public CommunityStatsDto stats() {
        return new CommunityStatsDto(
                teamRepo.findTopFavoriteTeams(),
                playerRepo.findTopFavoritePlayers(),
                predictionRepo.findMatchConsensus()
        );
    }
}
