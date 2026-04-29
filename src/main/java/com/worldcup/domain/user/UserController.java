package com.worldcup.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepo;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal Long userId) {
        return userRepo.findById(userId)
                .map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @PutMapping("/me")
    public ResponseEntity<UserDto> update(
            @AuthenticationPrincipal Long userId,
            @RequestBody UpdateRequest req) {
        userRepo.updateProfile(userId, req.displayName(), req.avatarUrl());
        return userRepo.findById(userId)
                .map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    record UpdateRequest(String displayName, String avatarUrl) {}
}
