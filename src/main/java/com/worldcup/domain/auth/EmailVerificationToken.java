package com.worldcup.domain.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "email_verification_tokens")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String token;
    private OffsetDateTime expiresAt;

    @Setter
    private Boolean used;

    private OffsetDateTime createdAt;
}
