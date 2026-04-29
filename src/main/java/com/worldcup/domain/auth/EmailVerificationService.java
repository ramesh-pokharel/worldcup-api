package com.worldcup.domain.auth;

import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    @Transactional
    public void sendVerification(User user) {
        tokenRepo.deleteByUserId(user.getId());

        String code = generateCode();
        tokenRepo.save(EmailVerificationToken.builder()
                .userId(user.getId())
                .token(code)
                .used(false)
                .expiresAt(OffsetDateTime.now().plusMinutes(15))
                .createdAt(OffsetDateTime.now())
                .build());

        emailService.sendVerificationEmail(user.getEmail(), user.getDisplayName(), code);
    }

    @Transactional
    public void verify(String email, String code) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired code"));

        if (Boolean.TRUE.equals(user.getIsVerified()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already verified");

        EmailVerificationToken vt = tokenRepo.findByUserIdAndTokenAndUsedFalse(user.getId(), code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired code"));

        if (vt.getExpiresAt().isBefore(OffsetDateTime.now()))
            throw new ResponseStatusException(HttpStatus.GONE, "Code has expired — request a new one");

        vt.setUsed(true);
        tokenRepo.save(vt);
        userRepo.markVerified(user.getId());
    }

    @Transactional
    public void resend(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsVerified()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already verified");

        sendVerification(user);
    }

    @Transactional
    public void resendByEmail(String email) {
        userRepo.findByEmail(email).ifPresent(user -> {
            if (!Boolean.TRUE.equals(user.getIsVerified())) {
                sendVerification(user);
            }
        });
    }
}
