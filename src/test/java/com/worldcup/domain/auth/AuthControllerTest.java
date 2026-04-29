package com.worldcup.domain.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldcup.config.AppProperties;
import com.worldcup.config.SecurityConfig;
import com.worldcup.domain.user.UserDto;
import com.worldcup.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties(AppProperties.class)
@TestPropertySource(properties = {
    "DATABASE_URL=placeholder",
    "app.jwt.secret=dGVzdHNlY3JldGtleWZvcnVuaXR0ZXN0aW5ncHVycG9zZXM=",
    "app.jwt.access-expiry-ms=900000",
    "app.jwt.refresh-expiry-ms=604800000",
    "app.cors.allowed-origins=http://localhost:5173",
    "app.cookie.secure=false",
    "app.mail.from=noreply@test.local",
    "app.mail.frontend-url=http://localhost:5173"
})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean JwtService jwtService;
    @MockitoBean AuthService authService;
    @MockitoBean EmailVerificationService emailVerificationService;

    private static final String USER_TOKEN = "test-user-token";
    private static final Long   USER_ID    = 1L;

    private static final UserDto SAMPLE_USER =
        new UserDto(USER_ID, "user@test.com", "Test User", null, "USER");

    @BeforeEach
    void setupJwt() {
        lenient().when(jwtService.isValid(USER_TOKEN)).thenReturn(true);
        lenient().when(jwtService.userId(USER_TOKEN)).thenReturn(USER_ID);
        lenient().when(jwtService.role(USER_TOKEN)).thenReturn("USER");
    }

    // ---- register ----

    @Test
    void register_validRequest_returns200WithUserDtoAndSetsCookies() throws Exception {
        when(authService.register(anyString(), anyString()))
            .thenReturn(new AuthService.AuthResult(SAMPLE_USER, "acc-tok", "ref-tok"));

        var result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"user@test.com","password":"secret123"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("user@test.com"))
            .andReturn();

        List<String> cookies = result.getResponse().getHeaders("Set-Cookie");
        assertThat(cookies).anySatisfy(h -> assertThat(h).contains("access_token=acc-tok"));
        assertThat(cookies).anySatisfy(h -> assertThat(h).contains("refresh_token=ref-tok"));
        assertThat(cookies).allSatisfy(h -> assertThat(h).containsIgnoringCase("HttpOnly"));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"not-an-email","password":"secret123"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"user@test.com","password":"short"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(authService.register(anyString(), anyString()))
            .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"dup@test.com","password":"secret123"}
                    """))
            .andExpect(status().isConflict());
    }

    // ---- login ----

    @Test
    void login_validCredentials_returns200AndSetsCookies() throws Exception {
        when(authService.login(anyString(), anyString()))
            .thenReturn(new AuthService.AuthResult(SAMPLE_USER, "acc-tok", "ref-tok"));

        var result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"user@test.com","password":"secret123"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(USER_ID))
            .andReturn();

        List<String> cookies = result.getResponse().getHeaders("Set-Cookie");
        assertThat(cookies).anySatisfy(h -> assertThat(h).contains("access_token="));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.login(anyString(), anyString()))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"x@y.com","password":"wrongpass1"}
                    """))
            .andExpect(status().isUnauthorized());
    }

    // ---- logout ----

    @Test
    void logout_clearsAccessAndRefreshCookies() throws Exception {
        var result = mockMvc.perform(delete("/api/auth/logout")
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isOk())
            .andReturn();

        List<String> cookies = result.getResponse().getHeaders("Set-Cookie");
        assertThat(cookies).anySatisfy(h ->
            assertThat(h).contains("access_token=").contains("Max-Age=0"));
        assertThat(cookies).anySatisfy(h ->
            assertThat(h).contains("refresh_token=").contains("Max-Age=0"));
    }

    // ---- me ----

    @Test
    void me_authenticated_returnsUserDto() throws Exception {
        when(authService.me(USER_ID)).thenReturn(SAMPLE_USER);

        mockMvc.perform(get("/api/auth/me")
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    void me_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    // ---- email verification ----

    @Test
    void verifyEmail_validToken_returns200AndDelegatesToService() throws Exception {
        mockMvc.perform(get("/api/auth/verify-email").param("token", "abc123"))
            .andExpect(status().isOk());

        verify(emailVerificationService).verify("abc123");
    }

    @Test
    void verifyEmail_invalidToken_returns400() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token"))
            .when(emailVerificationService).verify(anyString());

        mockMvc.perform(get("/api/auth/verify-email").param("token", "bad"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyEmail_expiredToken_returns410() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.GONE, "Token expired"))
            .when(emailVerificationService).verify(anyString());

        mockMvc.perform(get("/api/auth/verify-email").param("token", "expired"))
            .andExpect(status().isGone());
    }

    // ---- resend verification ----

    @Test
    void resendVerification_authenticated_returns200AndDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/auth/resend-verification")
                .cookie(new Cookie("access_token", USER_TOKEN)))
            .andExpect(status().isOk());

        verify(emailVerificationService).resend(USER_ID);
    }

    @Test
    void resendVerification_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/resend-verification"))
            .andExpect(status().isUnauthorized());
    }
}
