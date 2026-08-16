package com.worldcup.domain.reference;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/stadiums")
@RequiredArgsConstructor
public class StadiumController {

    private final StadiumRepository stadiumRepository;

    record StadiumDto(Long id, String name, String city) {}

    @GetMapping
    public List<StadiumDto> getAll() {
        return stadiumRepository.findAll().stream()
            .sorted(Comparator.comparing(Stadium::getName))
            .map(s -> new StadiumDto(s.getId(), s.getName(), s.getCity()))
            .toList();
    }
}
