package com.worldcup.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserRepository userRepo;
    private final UserMapper userMapper;

    record UpdateUserRequest(String role, Boolean isActive) {}

    @GetMapping
    public ResponseEntity<Page<AdminUserDto>> list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(userRepo.findAll(pageable).map(userMapper::toAdminDto));
    }

    @Transactional
    @PatchMapping("/{id}")
    public ResponseEntity<AdminUserDto> update(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId,
            @RequestBody UpdateUserRequest req) {

        if (id.equals(currentUserId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot modify your own account");

        if (!userRepo.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        if (req.role() != null)     userRepo.updateRole(id, req.role());
        if (req.isActive() != null) userRepo.updateActive(id, req.isActive());

        return ResponseEntity.ok(userMapper.toAdminDto(userRepo.findById(id).orElseThrow()));
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {

        if (id.equals(currentUserId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete your own account");

        if (!userRepo.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        userRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
