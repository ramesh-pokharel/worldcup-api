package com.worldcup.config;

import com.worldcup.domain.user.User;
import com.worldcup.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email:admin@worldcup2026.local}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@2026}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepo.existsByEmail(adminEmail)) {
            log.debug("Admin already exists, skipping seed");
            return;
        }

        String username = deriveUsername(adminEmail);

        userRepo.save(User.builder()
                .username(username)
                .email(adminEmail)
                .passwordHash(encoder.encode(adminPassword))
                .displayName("Admin")
                .role("ADMIN")
                .isVerified(true)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());

        log.info("Admin user seeded: {}", adminEmail);
    }

    private String deriveUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
        if (base.length() > 45) base = base.substring(0, 45);
        String candidate = base;
        for (int i = 2; userRepo.existsByUsername(candidate); i++)
            candidate = base + i;
        return candidate;
    }
}
