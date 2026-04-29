package com.worldcup.domain.player;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerRepository playerRepo;
    private final PlayerMapper playerMapper;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {

        if (size > 50) size = 50;
        Page<Player> result = playerRepo.findByTeamIdOrderByPositionAscShirtNumberAsc(
                teamId, PageRequest.of(page, size));

        return ResponseEntity.ok(Map.of(
            "content",        playerMapper.toDtoList(result.getContent()),
            "page",           page,
            "size",           size,
            "total_elements", result.getTotalElements(),
            "total_pages",    result.getTotalPages()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDto> getById(@PathVariable Long id) {
        return playerRepo.findById(id)
                .map(playerMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
