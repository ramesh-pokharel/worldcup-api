package com.worldcup.domain.team;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamRepository teamRepo;
    private final TeamMapper teamMapper;

    @GetMapping
    public List<TeamDto> list(@RequestParam(required = false) String group) {
        List<Team> teams = group != null
                ? teamRepo.findByGroupIdIgnoreCaseOrderByFifaRankingAsc(group)
                : teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc();
        return teamMapper.toDtoList(teams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> getById(@PathVariable Long id) {
        return teamRepo.findById(id)
                .map(teamMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
