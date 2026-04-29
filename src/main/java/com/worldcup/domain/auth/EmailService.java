package com.worldcup.domain.auth;

import com.worldcup.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final AppProperties props;

    @Async
    public void sendVerificationEmail(String to, String username, String code) {
        String verifyUrl = props.mail().frontendUrl() + "/verify-email";
        String html = buildVerificationHtml(username, code, verifyUrl);

        Map<String, Object> body = Map.of(
            "sender",      Map.of("name", "World Cup 2026", "email", props.mail().from()),
            "to",          List.of(Map.of("email", to)),
            "subject",     "Your World Cup 2026 verification code",
            "htmlContent", html
        );

        try {
            RestClient.create()
                .post()
                .uri(BREVO_URL)
                .header("api-key", props.mail().brevoApiKey())
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .toBodilessEntity();
            log.info("Verification code email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
        }
    }

    private String buildVerificationHtml(String username, String code, String verifyUrl) {
        return """
            <html><body style="font-family:sans-serif;max-width:480px;margin:auto;padding:24px">
              <h2 style="color:#1a1a2e">⚽ World Cup 2026</h2>
              <p>Hi <strong>%s</strong>,</p>
              <p>Thanks for joining! Use the code below to verify your email address.</p>
              <div style="margin:32px 0;text-align:center">
                <div style="display:inline-block;background:#f4f4f4;border-radius:12px;padding:20px 40px">
                  <p style="margin:0 0 8px;color:#666;font-size:13px">Your verification code</p>
                  <p style="margin:0;font-size:40px;font-weight:bold;letter-spacing:12px;color:#1a1a2e">%s</p>
                </div>
              </div>
              <p>Enter this code at <a href="%s">%s</a>.</p>
              <p style="color:#666;font-size:13px">
                This code expires in <strong>15 minutes</strong>. If you didn't create an account, ignore this email.
              </p>
            </body></html>
            """.formatted(username, code, verifyUrl, verifyUrl);
    }
}
