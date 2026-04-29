package com.worldcup.domain.auth;

import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserDto;
import com.worldcup.domain.user.UserMapper;
import com.worldcup.domain.user.UserRepository;
import com.worldcup.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepo;
    private final RefreshTokenRepository tokenRepo;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;
    private final EmailVerificationService emailVerificationService;

    public record AuthResult(UserDto user, String accessToken, String refreshToken) {}

    @Transactional
    public void register(String email, String password) {
        if (userRepo.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");

        String username = uniqueUsername(email);
        User user = userRepo.save(User.builder()
                .username(username)
                .email(email)
                .passwordHash(encoder.encode(password))
                .displayName(username)
                .role("USER")
                .isVerified(false)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());

        // Soft failure — a mail outage must not block account creation
        try {
            emailVerificationService.sendVerification(user);
        } catch (Exception e) {
            log.error("Failed to queue verification email for user {}: {}", user.getId(), e.getMessage());
        }
    }

    @Transactional
    public AuthResult login(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!encoder.matches(password, user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        if (!Boolean.TRUE.equals(user.getIsVerified()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED");
        userRepo.updateLastLogin(user.getId(), OffsetDateTime.now());
        return issue(user);
    }

    public String refresh(String rawToken) {
        if (rawToken == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No refresh token");
        RefreshToken rt = tokenRepo.findByToken(rawToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));
        if (rt.getExpiresAt().isBefore(OffsetDateTime.now())) {
            tokenRepo.deleteById(rt.getId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        User user = userRepo.findById(rt.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return jwtService.generate(user.getId(), user.getEmail(), user.getRole());
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken != null) tokenRepo.deleteByToken(rawToken);
    }

    public UserDto me(Long userId) {
        return userRepo.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private AuthResult issue(User user) {
        String access  = jwtService.generate(user.getId(), user.getEmail(), user.getRole());
        String refresh = UUID.randomUUID().toString();
        tokenRepo.save(RefreshToken.builder()
                .userId(user.getId())
                .token(refresh)
                .expiresAt(OffsetDateTime.now().plusSeconds(604800))
                .createdAt(OffsetDateTime.now())
                .build());
        return new AuthResult(userMapper.toDto(user), access, refresh);
    }

    private String uniqueUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
        if (base.length() > 45) base = base.substring(0, 45);
        String candidate = base;
        for (int i = 2; userRepo.existsByUsername(candidate); i++)
            candidate = base + i;
        return candidate;
    }
}
