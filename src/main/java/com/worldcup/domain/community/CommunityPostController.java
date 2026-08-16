package com.worldcup.domain.community;

import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostRepository postRepo;
    private final UserRepository userRepo;

    record CreatePostRequest(@NotBlank @Size(max = 1000) String body) {}

    @GetMapping
    public List<CommunityPostDto> list() {
        return postRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(p -> new CommunityPostDto(
                        p.getId(),
                        p.getUser().getId(),
                        p.getUser().getDisplayName(),
                        p.getBody(),
                        p.getCreatedAt()))
                .toList();
    }

    @PostMapping
    public ResponseEntity<CommunityPostDto> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreatePostRequest req) {

        User user = userRepo.findById(userId).orElseThrow();
        CommunityPost saved = postRepo.save(CommunityPost.builder()
                .user(user)
                .body(req.body())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(new CommunityPostDto(
                saved.getId(), saved.getUser().getId(),
                saved.getUser().getDisplayName(), saved.getBody(), saved.getCreatedAt()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {

        CommunityPost post = postRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!post.getUser().getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        postRepo.delete(post);
        return ResponseEntity.noContent().build();
    }
}
