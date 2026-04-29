package com.worldcup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// WHY @ConfigurationProperties: binds the entire `app.*` block from application.yml
// into a single typed object — no scattered @Value annotations across the codebase.
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    Jwt jwt,
    Cors cors,
    Cookie cookie,
    Mail mail
) {
    public record Jwt(String secret, long accessExpiryMs, long refreshExpiryMs) {}
    public record Cors(String allowedOrigins) {}
    public record Cookie(boolean secure) {}
    public record Mail(String from, String frontendUrl, String brevoApiKey) {}
}
