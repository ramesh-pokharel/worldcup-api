package com.worldcup;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
 * WHY @SpringBootTest(webEnvironment = NONE):
 *   Loads the full application context (including Flyway migrations) without
 *   starting an HTTP server. Flyway runs V1–V5 once at startup, so all seed
 *   data (teams, players, matches) is available across all test classes.
 *
 * WHY @Transactional on the class:
 *   Each test method runs in a transaction that is rolled back after the test.
 *   User/comment/prediction data inserted by tests disappears automatically —
 *   no manual teardown needed. Flyway seed data is unaffected (committed outside
 *   any test transaction).
 *
 * WHY @ServiceConnection:
 *   Spring Boot wires the Testcontainers Postgres URL directly into the
 *   datasource autoconfiguration. We don't need @DynamicPropertySource or
 *   hardcoded URLs.
 *
 * WHY static container:
 *   JUnit 5 creates one container instance per test class by default.
 *   Declaring it static lets Testcontainers reuse the same container across
 *   all subclasses — one startup cost for the whole test run.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        // DATABASE_URL placeholder — @ServiceConnection overrides spring.datasource.*
        // but the yml still needs ${DATABASE_URL} to resolve during property binding.
        "DATABASE_URL=placeholder",
        "app.jwt.secret=dGVzdHNlY3JldGtleWZvcnVuaXR0ZXN0aW5ncHVycG9zZXM=",
        "app.jwt.access-expiry-ms=900000",
        "app.jwt.refresh-expiry-ms=604800000",
        "app.cors.allowed-origins=http://localhost:5173",
        "app.cookie.secure=false"
    }
)
@Testcontainers
@Transactional
public abstract class AbstractRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");
}
