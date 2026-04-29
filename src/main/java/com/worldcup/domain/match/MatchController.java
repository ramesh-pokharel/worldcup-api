package com.worldcup.domain.match;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchRepository matchRepo;
    private final MatchMapper matchMapper;

    @GetMapping
    public List<MatchDto> list(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String status) {

        if (stage  != null) return matchMapper.toDtoList(matchRepo.findByStageOrderByScheduledAtAsc(MatchStage.valueOf(stage.toUpperCase())));
        if (group  != null) return matchMapper.toDtoList(matchRepo.findByGroupIdOrderByScheduledAtAsc(group.toUpperCase()));
        if (status != null) return matchMapper.toDtoList(matchRepo.findByStatusOrderByScheduledAtAsc(MatchStatus.valueOf(status.toUpperCase())));
        return matchMapper.toDtoList(matchRepo.findAllByOrderByScheduledAtAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDto> getById(@PathVariable Long id) {
        return matchRepo.findById(id)
                .map(matchMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
