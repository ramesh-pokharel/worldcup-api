package com.worldcup.domain.auth;

import com.worldcup.config.AppProperties;
import com.worldcup.domain.user.UserDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final AppProperties props;

    // ---- request bodies as local records (no separate files for tiny DTOs) ----
    record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password
    ) {}

    record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password
    ) {}

    record UpdateProfileRequest(String displayName, String avatarUrl) {}

    record ResendRequest(@NotBlank @Email String email) {}

    record VerifyEmailRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 6) String code
    ) {}

    // ---- endpoints ----

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req.email(), req.password());
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest req,
                                         HttpServletResponse res) {
        var result = authService.login(req.email(), req.password());
        setCookies(res, result.accessToken(), result.refreshToken());
        return ResponseEntity.ok(result.user());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refresh_token", required = false) String token,
            HttpServletResponse res) {
        String newAccess = authService.refresh(token);
        addCookie(res, "access_token", newAccess, 900);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String token,
            HttpServletResponse res) {
        authService.logout(token);
        clearCookies(res);
        return ResponseEntity.ok().build();
    }

    // Used by the React app on mount to rehydrate the auth state
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal Long userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(authService.me(userId));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest req) {
        emailVerificationService.verify(req.email(), req.code());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody ResendRequest req) {
        emailVerificationService.resendByEmail(req.email());
        return ResponseEntity.ok().build();
    }

    // ---- cookie helpers ----

    private void setCookies(HttpServletResponse res, String access, String refresh) {
        addCookie(res, "access_token",  access,  900);
        addCookie(res, "refresh_token", refresh, 604800);
    }

    private void addCookie(HttpServletResponse res, String name, String value, int maxAge) {
        // WHY manual Set-Cookie header: Jakarta Cookie API doesn't support SameSite attribute yet.
        // WHY SameSite=None in prod: frontend and backend are on different railway.app subdomains,
        // which the browser treats as cross-site. SameSite=None;Secure is required to allow the
        // cookie to be sent on cross-origin fetch requests.
        boolean secure = props.cookie().secure();
        String sameSite = secure ? "None" : "Lax";
        res.addHeader("Set-Cookie", String.format(
            "%s=%s; Max-Age=%d; Path=/; HttpOnly; SameSite=%s%s",
            name, value, maxAge, sameSite,
            secure ? "; Secure" : ""));
    }

    private void clearCookies(HttpServletResponse res) {
        addCookie(res, "access_token",  "", 0);
        addCookie(res, "refresh_token", "", 0);
    }
}
