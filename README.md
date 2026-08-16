# worldcup-api

Spring Boot REST API for a social World Cup 2026 fan platform — teams, players, matches, predictions, comments, favorites, a community wall, and admin tooling for running live match/tournament results.

For a full deep-dive (schema, request flows, every endpoint, incidents fixed), see [`../WORLDCUP_API_SUMMARY.md`](../WORLDCUP_API_SUMMARY.md).

## Stack

Java 25 · Spring Boot 4.0.5 · PostgreSQL 17 · Flyway · Spring Data JPA / Hibernate 7 · MapStruct · JWT (HttpOnly cookies) · Brevo (transactional email) · Maven · JUnit 5 + Testcontainers

## Prerequisites

- JDK 25
- Docker (for Postgres, and for `docker compose up`)
- A [Brevo](https://www.brevo.com/) account + API key (transactional email is required — see below)

## Quick start

```bash
cp .env.example .env
```

Fill in at minimum:

| Variable | Notes |
|---|---|
| `JWT_SECRET` | Base64-encoded, ≥ 32 bytes |
| `DB_USER` / `DB_PASSWORD` | Defaults to `postgres`/`postgres` if unset |
| `BREVO_API_KEY` | **Required** — `AppProperties.Mail.brevoApiKey` is `@NotBlank`; the app fails to boot without it. Get one from Brevo → SMTP & API → API Keys, and verify a sender email under Senders & Domains |
| `MAIL_FROM` | The Brevo-verified sender address |

Then:

```bash
docker compose up
```

This starts:
- `db` — PostgreSQL 17 on `5432`
- `mailhog` — present in the compose file but currently unused (email sends via Brevo's HTTPS API, not SMTP)
- `api` — Spring Boot on `8080`, waits for `db`'s healthcheck

Flyway runs all migrations automatically on boot. An admin account is seeded on first boot (`admin@worldcup2026.local` / `Admin@2026` by default — override with `APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD`).

### Running without Docker

```bash
mvn spring-boot:run
```
Requires a running Postgres reachable via `DATABASE_URL`, and the same env vars above set in your shell.

## Auth model

Tokens are issued as `HttpOnly` cookies (`access_token`, 15 min; `refresh_token`, 7 days) — not `Authorization: Bearer` headers. If you're testing with Postman, its cookie jar handles this automatically; no manual token copying needed. If you're calling from a browser-based client, requests need `withCredentials: true`.

Registration no longer logs you in automatically — verify your email (6-digit code, POST `/api/auth/verify-email`) before `POST /api/auth/login` will succeed.

## Tests

```bash
mvn test
```

Runs repository tests (real Postgres via Testcontainers) and controller tests (`@WebMvcTest` with mocked services). See [`WORLDCUP_API_SUMMARY.md` §17](../WORLDCUP_API_SUMMARY.md#17-testing-strategy) for what's covered and what isn't.

## API surface

All endpoints, request/response shapes, and admin capabilities are documented in [`WORLDCUP_API_SUMMARY.md`](../WORLDCUP_API_SUMMARY.md). Quick orientation:

- Public reads: `/api/teams`, `/api/players`, `/api/matches`, `/api/stadiums`, `/api/comments`, `/api/leaderboard`, `/api/community/**`, `/api/tournament-predictions/all`
- Auth required: `/api/users/me`, `/api/favorites/**`, `/api/comments` (write), `/api/predictions`, `/api/tournament-predictions`, `/api/community/posts` (write)
- Admin only (`role = ADMIN`): `/api/admin/**` — match results/bracket/scheduling, user management, tournament result & scoring

## Known gaps

No team/player admin CRUD (direct SQL only), no password reset/change flow, no automatic knockout bracket advancement. Full list in [`WORLDCUP_API_SUMMARY.md` §20](../WORLDCUP_API_SUMMARY.md#20-known-gaps).
