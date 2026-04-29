package com.worldcup;

import com.worldcup.domain.user.User;

import java.time.OffsetDateTime;

/**
 * Builds unsaved entity instances for use in tests.
 * Callers pass the result to a repository's save() method.
 */
public class TestDataFactory {

    public static User.UserBuilder user(String suffix) {
        return User.builder()
                .username("user_" + suffix)
                .email("user_" + suffix + "@test.com")
                .passwordHash("$2a$10$irrelevantHashForTests")
                .displayName("User " + suffix)
                .role("USER")
                .isVerified(false)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now());
    }
}
