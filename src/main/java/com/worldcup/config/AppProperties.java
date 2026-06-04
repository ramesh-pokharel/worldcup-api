package com.worldcup.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// WHY @ConfigurationProperties: binds the entire `app.*` block from application.yml
// into a single typed object — no scattered @Value annotations across the codebase.
@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
    Jwt jwt,
    Cors cors,
    Cookie cookie,
    Mail mail
) {
    public record Jwt(@NotBlank String secret, @Positive long accessExpiryMs, @Positive long refreshExpiryMs) {}
    public record Cors(@NotBlank String allowedOrigins) {}
    public record Cookie(boolean secure) {}
    public record Mail(String from, String frontendUrl, @NotBlank String brevoApiKey) {}
}
