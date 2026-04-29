package com.worldcup.domain.favorite;

import com.worldcup.domain.player.Player;
import com.worldcup.domain.player.PlayerDto;
import com.worldcup.domain.player.PlayerMapper;
import com.worldcup.domain.player.PlayerRepository;
import com.worldcup.domain.team.Team;
import com.worldcup.domain.team.TeamDto;
import com.worldcup.domain.team.TeamMapper;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final UserRepository userRepo;
    private final TeamRepository teamRepo;
    private final PlayerRepository playerRepo;
    private final TeamMapper teamMapper;
    private final PlayerMapper playerMapper;

    // ---- Teams ----

    @GetMapping("/teams")
    public List<TeamDto> myTeams(@AuthenticationPrincipal Long userId) {
        return teamMapper.toDtoList(teamRepo.findFavoritesByUserId(userId));
    }

    @Transactional
    @PostMapping("/teams/{teamId}")
    public ResponseEntity<Map<String, Boolean>> addTeam(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long teamId) {
        User user = userRepo.findByIdWithFavoriteTeams(userId).orElseThrow();
        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        user.getFavoriteTeams().add(team);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("favorited", true));
    }

    @Transactional
    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<Map<String, Boolean>> removeTeam(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long teamId) {
        User user = userRepo.findByIdWithFavoriteTeams(userId).orElseThrow();
        user.getFavoriteTeams().removeIf(t -> t.getId().equals(teamId));
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("favorited", false));
    }

    // ---- Players ----

    @GetMapping("/players")
    public List<PlayerDto> myPlayers(@AuthenticationPrincipal Long userId) {
        return playerMapper.toDtoList(playerRepo.findFavoritesByUserId(userId));
    }

    @Transactional
    @PostMapping("/players/{playerId}")
    public ResponseEntity<Map<String, Boolean>> addPlayer(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long playerId) {
        User user = userRepo.findByIdWithFavoritePlayers(userId).orElseThrow();
        Player player = playerRepo.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
        user.getFavoritePlayers().add(player);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("favorited", true));
    }

    @Transactional
    @DeleteMapping("/players/{playerId}")
    public ResponseEntity<Map<String, Boolean>> removePlayer(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long playerId) {
        User user = userRepo.findByIdWithFavoritePlayers(userId).orElseThrow();
        user.getFavoritePlayers().removeIf(p -> p.getId().equals(playerId));
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("favorited", false));
    }
}
