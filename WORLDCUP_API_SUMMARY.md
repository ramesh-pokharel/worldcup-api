# World Cup 2026 — Backend Summary

A deep-dive into what every part of this backend does, why it was built that way, and how the pieces fit together. Covers `./worldcup-api` only — see `WORLDCUP_UI_PROGRESS.md` for the React frontend.

---

## Table of Contents

1. [What this backend is](#1-what-this-backend-is)
2. [Tech stack and why](#2-tech-stack-and-why)
3. [Database schema](#3-database-schema)
4. [How a request flows end-to-end](#4-how-a-request-flows-end-to-end)
5. [Authentication and security](#5-authentication-and-security)
6. [Email verification (OTP)](#6-email-verification-otp)
7. [Teams, players, stadiums (read-only catalog)](#7-teams-players-stadiums-read-only-catalog)
8. [Matches](#8-matches)
9. [Favorites](#9-favorites)
10. [Comments and likes](#10-comments-and-likes)
11. [Score predictions and leaderboard](#11-score-predictions-and-leaderboard)
12. [Tournament predictions and scoring](#12-tournament-predictions-and-scoring)
13. [Community hub (stats + wall posts)](#13-community-hub-stats--wall-posts)
14. [Admin capabilities](#14-admin-capabilities)
15. [Error handling](#15-error-handling)
16. [Configuration and environment variables](#16-configuration-and-environment-variables)
17. [Testing strategy](#17-testing-strategy)
18. [Docker and local development](#18-docker-and-local-development)
19. [Flyway migration history](#19-flyway-migration-history)
20. [Known gaps](#20-known-gaps)
21. [Production incidents fixed](#21-production-incidents-fixed)

---

## 1. What this backend is

This is a REST API for a social World Cup 2026 fan platform. Users can:

- Browse all 48 qualified teams, ~700+ players, 16 stadiums, and 104 matches
- Register an account, verify their email with a 6-digit code, and log in
- Mark favourite teams and players
- Submit score predictions before each match starts, and a once-per-tournament pick for winner/runner-up/top scorer
- Read and write comments on matches, teams, and players
- Post to a global community message wall (separate from match/team/player comments)
- See a live leaderboard ranked by prediction accuracy (match points + tournament points)

Admins can update live match scores, assign knockout bracket teams, reschedule fixtures, manage users, and score the tournament-wide prediction once the real tournament ends.

The entire API speaks JSON. All responses use `snake_case` field names (configured globally in Jackson). Error responses follow [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807) format.

---

## 2. Tech stack and why

| Layer | Technology | Why |
|---|---|---|
| Language | Java 25 | Records, pattern matching, text blocks |
| Framework | Spring Boot 4.0.5 | Production-proven, rich ecosystem, auto-configuration |
| Persistence | Spring Data JPA + Hibernate 7 | Object-relational mapping, derived queries, EntityGraph |
| Database | PostgreSQL 17 (Docker) / Neon (prod) | Custom ENUMs, partial indexes, production-grade on a free tier |
| Migrations | Flyway (via `spring-boot-starter-flyway`) | Schema changes are versioned and reproducible; never modify tables manually |
| DTO mapping | MapStruct 1.6.3 | Compile-time, zero-reflection mapper; no runtime overhead |
| Auth | JWT (`jjwt` 0.13.0) in HttpOnly cookies | Tokens can't be read by JavaScript, preventing XSS theft |
| Email | Brevo HTTP API via `RestClient` | HTTPS request, not SMTP — bypasses Railway's port blocking (see [§21](#21-production-incidents-fixed)) |
| Build | Maven | Stable, widely supported |
| Tests | JUnit 5 + Testcontainers 1.20.4 + MockMvc | Real DB in repo tests; Spring Security slice in controller tests |

### Why JPA instead of JDBC?

Spring Data JPA lets us write relationship traversal (e.g. `team.country.confederation.name`) without manual JOINs in every query. The N+1 risk is controlled by `@EntityGraph` annotations that compile the correct JOIN into SQL at query time.

### Why MapStruct instead of manual mapping?

MapStruct generates mapper code at compile time via annotation processing. If an entity field is renamed, the build fails immediately rather than silently returning `null` at runtime.

### Why cookies instead of `Authorization: Bearer` headers?

`HttpOnly` cookies are invisible to JavaScript, so an XSS payload can't steal the token from `document.cookie`. `SameSite` policy is environment-driven (`Lax` locally, `None` in prod — see [§5](#5-authentication-and-security)) to support the cross-subdomain Vercel↔Railway deployment while still limiting CSRF exposure locally.

---

## 3. Database schema

### Reference tables (seed data, read-only)

```
confederations  → id, code (UEFA / CONMEBOL / CAF / AFC / CONCACAF / OFC), name
countries       → id, name, code_iso2, code_iso3, flag_emoji, confederation_id
stadiums        → id, name, city, country (USA/Canada/Mexico), capacity, lat, lng
```

### Tournament tables

```
teams           → id, country_id, fifa_ranking, group_id (A-L), manager, kit colors
players         → id, team_id, name, position (GK/CB/CM/ST/…), shirt_number, club, caps, goals
matches         → id, match_number (1-104), stage (GROUP/R16/QF/SF/FINAL…),
                  home_team_id, away_team_id, stadium_id, scheduled_at,
                  status (SCHEDULED/LIVE/FINISHED/…),
                  home_score, away_score, home_score_pen, away_score_pen
match_events    → id, match_id, player_id, event_type (GOAL/YELLOW_CARD/…), minute
                  (table exists from V1; no Java entity/repository touches it — dead table)
```

### User tables

```
users                 → id, username, email, password_hash, display_name, avatar_url, role, is_verified
refresh_tokens        → id, user_id, token, expires_at
email_verification_tokens → id, user_id, token (6-digit OTP), expires_at, used
user_favorite_teams   → (user_id, team_id) composite PK join table
user_favorite_players → (user_id, player_id) composite PK join table
```

### Social / prediction tables

```
predictions            → id, user_id, match_id, predicted_home_score, predicted_away_score,
                          points_earned (0/1/3), UNIQUE(user_id, match_id)
prediction_leaderboard → user_id (PK = FK), total_points, exact_scores, correct_results, tournament_points
comments               → id, user_id, match_id|team_id|player_id (exactly one non-null),
                          parent_id (threading), body, is_deleted, like_count
comment_likes          → (user_id, comment_id) composite PK
tournament_predictions → id, user_id, predicted_winner_id, predicted_runner_up,
                          predicted_top_scorer, points_earned, UNIQUE(user_id)
tournament_result      → single-row table: actual_winner_id, actual_top_scorer_id
community_posts        → id, user_id, body (max 1000 chars), created_at
```

**Key database constraint:** the `comments` table has a CHECK constraint enforcing exactly one target:
```sql
CONSTRAINT one_target CHECK (
    (match_id IS NOT NULL)::int +
    (team_id  IS NOT NULL)::int +
    (player_id IS NOT NULL)::int = 1
)
```
Enforced at the database level even if application code has a bug — the application also validates this before saving.

**Custom PostgreSQL ENUMs:** `match_stage` and `match_status` are PostgreSQL ENUM types. Hibernate needs `@Column(columnDefinition = "match_stage")` to bind correctly. JPQL `UPDATE` can't express PostgreSQL's `::` cast, so native SQL with `CAST(:param AS enum_type)` is used instead (see [§21](#21-production-incidents-fixed)).

---

## 4. How a request flows end-to-end

What happens when an authenticated user calls `GET /api/matches/42`:

```
1. Browser sends HTTP request with Cookie: access_token=eyJhb...

2. JwtAuthFilter (runs before every request)
   - Reads the access_token cookie
   - Calls JwtService.isValid(token) → parses and verifies HMAC signature
   - Extracts "sub" (user id) and "role" claims
   - Puts UsernamePasswordAuthenticationToken(principal=7, authorities=[ROLE_USER])
     into Spring's SecurityContext

3. Spring Security checks authorization rules:
   GET /api/matches/** is permitAll() → no role check needed

4. MatchController.getById(42)
   - matchRepo.findById(42) fires one SQL query with JOINs
     (via @EntityGraph — no N+1)

5. matchMapper.toDto(match) — MapStruct-generated, flattens nested objects

6. Jackson serializes to JSON with snake_case names → HTTP 200
```

```json
{
  "id": 42, "match_number": 1, "stage": "GROUP", "group_id": "A",
  "team1_id": 3, "team1_name": "United States",
  "team2_id": 7, "team2_name": "Serbia",
  "stadium_name": "SoFi Stadium", "scheduled_at": "2026-06-11T19:00:00Z",
  "status": "SCHEDULED", "team1_score": null, "team2_score": null
}
```

---

## 5. Authentication and security

### Registration (`POST /api/auth/register`)

```
Request:  { "email": "fan@example.com", "password": "myPassword1" }
Response: 201 Created, empty body — NO cookies are set
```

**`AuthService.register()`:**
1. Check `userRepo.existsByEmail(email)` → 409 if already registered
2. Derive a unique username from the email prefix (append a number if taken)
3. Hash the password with BCrypt (cost factor 10)
4. Save the `User` entity — `isVerified = false`, `role = "USER"`
5. Call `emailVerificationService.sendVerification(user)` in a try/catch — a mail failure must not block account creation
6. Return `void` — **no tokens are issued at registration.** The user must verify their email and then log in explicitly. (This changed from an earlier version that auto-logged-in on register.)

### Login (`POST /api/auth/login`)

```
Request:  { "email": "fan@example.com", "password": "myPassword1" }
Response: 200 with user body, sets access_token + refresh_token cookies
```

`AuthService.login()`:
1. `encoder.matches(password, hash)` — a wrong password and a non-existent user both return the same 401 ("Invalid credentials") to prevent user enumeration
2. **If `!user.isVerified()` → throws `403 FORBIDDEN` with the exact message `EMAIL_NOT_VERIFIED`.** The frontend string-matches on this to show a "resend verification" banner instead of a generic error.
3. Otherwise issues both cookies as normal

| Token | Lifetime | Purpose |
|---|---|---|
| `access_token` JWT | 15 minutes (`JWT_ACCESS_EXPIRY_MS`) | Proves identity on each request |
| `refresh_token` UUID | 7 days (`JWT_REFRESH_EXPIRY_MS`) | Stored in DB; used only to mint a new access token |

### Token refresh (`POST /api/auth/refresh`)

Server looks up the `refresh_token` cookie value in the DB, checks `expiresAt`, and issues a new `access_token` cookie. The refresh token itself is not rotated.

### Logout (`DELETE /api/auth/logout`)

Deletes the refresh token row, then sets both cookies to `Max-Age=0`.

### Cookie flags — environment-driven

`AuthController.addCookie()`:
- `COOKIE_SECURE=false` (local) → `SameSite=Lax`, not marked `Secure`
- `COOKIE_SECURE=true` (prod) → `SameSite=None`, marked `Secure`

**Why:** the frontend (Vercel) and backend (Railway) are on different subdomains in production, so the cookie must be `SameSite=None; Secure` to be sent cross-site at all. Locally both run on `localhost`, so `Lax` is safer and works fine.

### JWT structure

```
Header:  { "alg": "HS256" }
Payload: { "sub": "12", "email": "fan@example.com", "role": "USER", "iat": ..., "exp": ... }
Signature: HMAC-SHA256(header.payload, JWT_SECRET)
```

### Role-based access

1. **URL-level** (`SecurityConfig.authorizeHttpRequests()`):
   ```
   /api/auth/**                                    → public
   /actuator/health                                → public
   GET /api/teams/**, /api/players/**,
   GET /api/matches/**, /api/stadiums/**,
   GET /api/comments/**, GET /api/predictions,
   GET /api/leaderboard, GET /api/community/**,
   GET /api/tournament-predictions/all             → public
   /api/admin/**                                   → ADMIN role required
   everything else                                 → must be authenticated
   ```
2. **Method-level** via `@PreAuthorize("hasRole('ADMIN')")` on individual controller methods, making intent explicit in code rather than only in config.

CORS: allows `GET, POST, PUT, PATCH, DELETE, OPTIONS`, `allowCredentials=true`, `maxAge=3600`.

---

## 6. Email verification (OTP)

Email verification moved from a **link + opaque token** design to a **6-digit code** design (migration V11). After registration, `is_verified = false`.

### Flow

```
Register
  ↓
EmailVerificationService.sendVerification(user)
  - Generate a 6-digit code via SecureRandom
  - Save EmailVerificationToken { userId, token=code, expiresAt = now+15min, used=false }
  - EmailService.sendVerificationEmail(...)   ← @Async, HTML email with the code shown large/bold

User enters the code in the frontend
  ↓
POST /api/auth/verify-email  { "email": "...", "code": "483920" }
  ↓
EmailVerificationService.verify(email, code)
  - Look up by (user_id, token) — UNIQUE(user_id, token), so the same 6-digit code CAN repeat
    across different users without conflict (V11 dropped the old global UNIQUE(token) constraint)
  - Check: exists (400), not already used (409), not expired (410)
  - Mark used = true; userRepo.markVerified(userId)
```

`POST /api/auth/resend-verification` `{ "email": "..." }` is **public** (not authenticated). `EmailVerificationService.resendByEmail()` silently no-ops on an unknown email — this is deliberate, to avoid leaking which emails have accounts.

### Why `@Async` on email sending?

Sending an email is a network round trip (~200–500ms). `@Async` (enabled via `@EnableAsync`) hands it to a background thread pool so the HTTP response returns immediately.

### Why swallow email exceptions at registration?

```java
try {
    emailVerificationService.sendVerification(user);
} catch (Exception e) {
    log.error("Failed to queue verification email for user {}: {}", user.getId(), e.getMessage());
}
```
The account was already saved; a temporarily-down mail provider must not roll back registration. The user can always resend.

### Email delivery: Brevo HTTP API (not SMTP)

`EmailService` sends via `RestClient` POSTing JSON to `https://api.brevo.com/v3/smtp/email`, authenticated with `BREVO_API_KEY`. This replaced an earlier JavaMailSender/SMTP implementation entirely — `AppProperties.Mail` now only has `from`, `frontendUrl`, `brevoApiKey` (no host/port/username/password fields at all). See [§21](#21-production-incidents-fixed) for why.

**⚠️ Stale local config:** `docker-compose.yml` and `.env` still define the old dead SMTP variables (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS`), and **neither sets `BREVO_API_KEY`**. Since `AppProperties.Mail.brevoApiKey` is `@NotBlank`, local `docker compose up` will fail Spring's configuration-properties validation unless `BREVO_API_KEY` is supplied out-of-band (shell env, or a local `.env` addition). The `mailhog` service in `docker-compose.yml` is now orphaned — nothing in the code sends to it anymore.

---

## 7. Teams, players, stadiums (read-only catalog)

Seeded by Flyway (V3, V4, V8, V9) and never modified through the API except indirectly (favorites don't mutate them).

### `GET /api/teams`
```
GET /api/teams          → all 48 teams, ordered by group then FIFA ranking
GET /api/teams?group=A  → only Group A teams
GET /api/teams/7        → single team
```
```json
{
  "id": 3, "name": "United States", "confederation": "CONCACAF",
  "country_name": "United States", "flag_emoji": "🇺🇸",
  "fifa_ranking": 13, "group_id": "A", "manager": "Mauricio Pochettino", "is_host": true
}
```
`@EntityGraph(attributePaths = {"country", "country.confederation"})` on `TeamRepository` avoids N+1 (65 queries → 1) when listing all teams.

### `GET /api/players`
```
GET /api/players?teamId=3&page=1&size=10
GET /api/players/42
```
```json
{
  "content": [{ "id": 42, "name": "Christian Pulisic", "team_name": "United States", "position": "LW", "shirt_number": 10 }],
  "page": 0, "size": 30, "total_elements": 26, "total_pages": 1
}
```
`size` is capped at 50. Players ordered by position then shirt number.

### `GET /api/stadiums`
Public, list-only, returns `id`, `name`, `city`. Used by the admin match-editing UI to populate a venue selector.

---

## 8. Matches

```
GET /api/matches                   → all 104 matches, ordered by scheduled_at
GET /api/matches?stage=QUARTER_FINAL
GET /api/matches?group=B
GET /api/matches?status=LIVE
GET /api/matches/1
```
```json
{
  "id": 1, "match_number": 1, "stage": "GROUP", "group_id": "A",
  "team1_id": 3, "team1_name": "United States",
  "team2_id": 7, "team2_name": "Serbia",
  "stadium_name": "SoFi Stadium", "stadium_city": "Los Angeles",
  "scheduled_at": "2026-06-11T19:00:00Z", "status": "SCHEDULED",
  "team1_score": null, "team2_score": null,
  "team1_score_pen": null, "team2_score_pen": null
}
```
Match fields use `team1`/`team2` naming in the API even though the DB columns are `home_team_id`/`away_team_id` — the JPA entity remaps this so the DB schema and the Java domain stay independent. Knockout matches (73–104) have real FIFA-schedule dates as of V10; `home_team_id`/`away_team_id` stay `NULL` until an admin assigns them as the bracket resolves (see [§14](#14-admin-capabilities)).

---

## 9. Favorites

Stored in join tables `user_favorite_teams` / `user_favorite_players` via `@ManyToMany` on `User`.

```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(name = "user_favorite_teams", ...)
@Builder.Default
private Set<Team> favoriteTeams = new HashSet<>();
```
`@Builder.Default` matters: without it, Lombok's `@Builder` leaves the field `null`, and `.add(team)` throws NPE.

`POST /api/favorites/teams/{teamId}`:
```
1. userRepo.findByIdWithFavoriteTeams(userId)   ← @EntityGraph, one query
2. user.getFavoriteTeams().add(team)            ← in-memory Set op
3. userRepo.save(user)                          ← Hibernate diffs the Set → INSERT
```
`@Transactional` on the controller keeps all three steps in one Hibernate session — without it, step 3 opens a fresh session that doesn't know what changed.

---

## 10. Comments and likes

Comments are polymorphic: each targets exactly one of `match_id` / `team_id` / `player_id`, enforced by the CHECK constraint in [§3](#3-database-schema) and validated in `CommentController` before saving. This is distinct from the newer **community wall posts** ([§13](#13-community-hub-stats--wall-posts)), which are global and untargeted.

### Listing (`GET /api/comments?matchId=42`)
```
1. commentRepo.findByMatchIdOrderByCreatedAtAsc(42)   → List<Comment>, User eager-loaded
2. likeRepo.findLikedCommentIds(viewerId, [ids...])   → one batched query for "did I like this"
3. commentMapper.toDto(comment, likedIds.contains(id))
```
Batching the liked-ID lookup avoids N queries for N comments.

### Soft delete
```sql
UPDATE comments SET is_deleted = true, body = null, updated_at = NOW() WHERE id = ? AND user_id = ?
```
`CommentMapper` renders `[deleted]` when `is_deleted = true`, preserving thread structure. The `AND user_id = ?` prevents deleting someone else's comment.

### Like/unlike idempotency
Both check `existsByUserIdAndCommentId` before acting; `decrementLikeCount` uses `GREATEST(like_count - 1, 0)` to prevent a negative counter under a race.

---

## 11. Score predictions and leaderboard

### `POST /api/predictions`
```json
Request:  { "match_id": 1, "predicted_team1_score": 2, "predicted_team2_score": 1 }
```
Guards: match exists (404), match is `SCHEDULED` (409 otherwise), no duplicate prediction for this match (409; UNIQUE constraint backs this up).

### `GET /api/predictions?matchId=1` — reveal mechanic
| Situation | What you see |
|---|---|
| SCHEDULED, not logged in | Empty list |
| SCHEDULED, logged in | Only your own prediction |
| LIVE or FINISHED | Everyone's predictions |

### Scoring (`POST /api/admin/leaderboard/recalculate`)
```java
if (predicted1 == actual1 && predicted2 == actual2) points = 3;      // exact
else if (signum(predicted1-predicted2) == signum(actual1-actual2)) points = 1; // right result
else points = 0;
```
`Integer.signum()` collapses win/draw/loss comparison into one expression. After scoring, the leaderboard is fully rebuilt (`deleteAll()` then re-aggregate) rather than incrementally updated. This same admin call also runs a second pass folding in `tournament_points` (see [§12](#12-tournament-predictions-and-scoring)).

### `GET /api/leaderboard`
Returns the **top 5** users (`findTop5ByOrderByTotalPointsDescExactScoresDesc` — not top 50; the frontend's `LeaderboardPage` subtitle says "top 5"), tie-broken by exact-score count.
```json
[{ "user_id": 5, "display_name": "Alice", "total_points": 42, "exact_scores": 9, "correct_results": 15, "tournament_points": 3 }]
```
`prediction_leaderboard.user_id` is both PK and FK via `@MapsId` — one row per user, cascades on user delete.

---

## 12. Tournament predictions and scoring

A separate one-per-user pick for the whole tournament: winner, runner-up, top scorer.

```
POST /api/tournament-predictions
{ "predicted_winner_id": 3, "predicted_runner_up_id": 7, "predicted_top_scorer_id": 42 }

PUT /api/tournament-predictions        ← same body, updates existing (a second POST returns 409)
GET /api/tournament-predictions/me     → includes points_earned once scored
GET /api/tournament-predictions/all    → public, everyone's picks + points (leaderboard-style reveal)
```

All three pick fields are optional (partial predictions allowed).

### Scoring (new — resolves the old "no tournament scoring" gap)
```
POST /api/admin/tournament-result           { "actual_winner_id": ..., "actual_top_scorer_id": ... }
  → saves the single-row tournament_result

POST /api/admin/leaderboard/score-tournament
  → awards 2 points for a correct winner pick, 1 point for a correct top-scorer pick,
    writes points_earned onto each tournament_predictions row,
    and folds the total into prediction_leaderboard.tournament_points
```
Run once after the real tournament concludes, in that order.

---

## 13. Community hub (stats + wall posts)

Two distinct features under `/api/community/**`, both public to read:

### `GET /api/community/stats`
Aggregated, non-personal stats for the homepage/community page:
```json
{
  "top_teams": [{ "team_id": 3, "team_name": "United States", "favorite_count": 210 }],
  "top_players": [{ "player_id": 42, "player_name": "Christian Pulisic", "favorite_count": 88 }],
  "prediction_consensus": [{ "match_id": 1, "team1_pct": 61, "team2_pct": 39 }]
}
```
Backed by three native-query projections: `TeamRepository.findTopFavoriteTeams()`, `PlayerRepository.findTopFavoritePlayers()`, `PredictionRepository.findMatchConsensus()` (restricted to `LIVE`/`FINISHED` matches only, so consensus can't be gamed pre-kickoff).

### `GET/POST/DELETE /api/community/posts`
A simple global message board, separate from per-match/team/player comments:
- `POST` (authenticated): `{ "body": "..." }`, max 1000 chars
- `GET` (public): list, most recent first
- `DELETE /{id}` (authenticated, own posts only)

Backed by the `community_posts` table (V12). No threading, no likes — intentionally simpler than the comments system.

---

## 14. Admin capabilities

All under `/api/admin/**`, `@PreAuthorize("hasRole('ADMIN')")`.

### `PATCH /api/admin/matches/{id}` — results, bracket, and scheduling
```json
{
  "status": "FINISHED",
  "team1_score": 2, "team2_score": 2,
  "team1_score_pen": 4, "team2_score_pen": 3,
  "team1_id": 12, "team2_id": 19,
  "scheduled_at": "2026-07-04T18:00:00Z", "stadium_id": 5
}
```
All fields optional/partial. Validation: match must exist (404); if `status = FINISHED`, both scores are required (400); penalty scores may only be set when the 90-minute scores are level (400).

This single endpoint now covers three jobs that used to be separate gaps: entering live results, manually assigning `team1_id`/`team2_id` as knockout brackets resolve (no automatic advancement logic exists — an admin does this by hand after each round), and rescheduling/re-venuing a fixture.

**Why native SQL for the status update:**
```sql
UPDATE matches SET status = CAST(:status AS match_status), ... WHERE id = :id
```
JPQL can't express the PostgreSQL enum cast; native SQL with `CAST(... AS ...)` (not the `::` shorthand — see [§21](#21-production-incidents-fixed)) hands the value straight to JDBC.

### User management
```
GET    /api/admin/users?page=0&size=20   → Page<AdminUserDto>, sorted by created_at desc
PATCH  /api/admin/users/{id}             { role?, is_active? }
DELETE /api/admin/users/{id}             → 204
```
`AdminUserDto`: `id, email, role, is_active, is_verified, created_at, last_login_at`. `UserRepository.updateRole`/`updateActive` issue targeted `UPDATE`s rather than load/modify/save. **Both PATCH and DELETE reject self-modification** — an admin can't demote, ban, or delete their own account.

### Tournament result & scoring
See [§12](#12-tournament-predictions-and-scoring).

### First admin account

No migration or endpoint creates the first admin. `AdminInitializer` (`ApplicationRunner`, `src/main/java/com/worldcup/config/AdminInitializer.java`) runs on every boot, idempotent (checks by email first):
- Default: `admin@worldcup2026.local` / `Admin@2026`
- Override via `APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD`
- BCrypt hash computed at runtime — never in source control

---

## 15. Error handling

All errors return RFC 7807 JSON:
```json
{ "type": "about:blank", "title": "Conflict", "status": 409, "detail": "Email already registered", "instance": "/api/auth/register" }
```
Validation errors add a field-level `errors` map:
```json
{ "status": 400, "title": "Validation Failed", "detail": "One or more fields are invalid",
  "errors": { "email": "must be a well-formed email address" } }
```

| Exception | HTTP Status | Trigger |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | `@Valid` fails on request body |
| `ResponseStatusException` | whatever status is set | Controller/service explicitly throws |
| `DataIntegrityViolationException` | 409 | DB UNIQUE violation (race-condition safety net) |
| `Exception` (catch-all) | 500 | Anything unhandled — hidden from client, logged server-side |

---

## 16. Configuration and environment variables

All app-specific config is typed via `AppProperties`:
```java
@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors, Cookie cookie, Mail mail) {
    public record Jwt(String secret, long accessExpiryMs, long refreshExpiryMs) {}
    public record Cors(String allowedOrigins) {}
    public record Cookie(boolean secure) {}
    public record Mail(String from, String frontendUrl, String brevoApiKey) {}
}
```

| Variable | Default | Purpose |
|---|---|---|
| `DATABASE_URL` | (required) | `jdbc:postgresql://host/db?user=...&password=...` |
| `DB_USER` / `DB_PASSWORD` | `postgres` / `postgres` | Local Docker Postgres creds |
| `JWT_SECRET` | (required) | Base64 HMAC secret, min 32 bytes |
| `JWT_ACCESS_EXPIRY_MS` | 900000 (15 min) | Access token lifetime |
| `JWT_REFRESH_EXPIRY_MS` | 604800000 (7 days) | Refresh token lifetime |
| `CORS_ORIGINS` | `http://localhost:5173` | Allowed frontend origin(s) |
| `COOKIE_SECURE` | `false` | `true` in prod → `SameSite=None; Secure` |
| `BREVO_API_KEY` | (required, `@NotBlank`) | Brevo transactional email API key — **not currently set in `docker-compose.yml`/`.env`; local boot will fail validation without it supplied out-of-band** |
| `MAIL_FROM` | — | From address in emails |
| `FRONTEND_URL` | `http://localhost:5173` | Base URL referenced in emails |
| `APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD` | seeded defaults | First admin account override |
| `PORT` | 8080 | HTTP server port |

**Dead variables still present in `docker-compose.yml`/`.env` from the pre-Brevo SMTP era** (no longer read by any code): `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS`. Safe to remove once `BREVO_API_KEY` is added in their place.

---

## 17. Testing strategy

### Repository tests (`AbstractRepositoryTest` subclasses)
`@SpringBootTest(webEnvironment = NONE)`, real Postgres via Testcontainers `@ServiceConnection`, Flyway runs the full migration set at container startup, `@Transactional` rolls back each test, `em.flush()`/`em.clear()` forces real DB round trips before assertions.

**7 classes:** Comment, CommentLike, Match, Player, Prediction/Leaderboard, Team, TournamentPrediction, User.

### Controller tests (`@WebMvcTest`)
Web layer only — controller, security filter, exception handler; `@Import(SecurityConfig.class)` loads the real auth rules; `@MockitoBean JwtService` simulates any user; all repos/services mocked.

**3 classes:** Auth, Comment, Prediction.

### Scoring unit tests (`ScoringTest`)
Plain JUnit 5 + AssertJ, `@ParameterizedTest` with 13 `@CsvSource` cases covering the exact scoring formula.

### Coverage gap
`UserAdminController`, `CommunityController`, `CommunityPostController`, and the tournament-scoring endpoints have **no test coverage** — all added after the last test-writing pass.

---

## 18. Docker and local development

```bash
cp .env.example .env   # fill in JWT_SECRET, DB_PASSWORD, and BREVO_API_KEY at minimum
docker compose up
```

Services:
- `db` — PostgreSQL 17 on port 5432
- `mailhog` — fake SMTP on 1025/8025 — **kept in the compose file for convenience but currently unused**; `EmailService` sends via Brevo HTTP, not SMTP, so nothing reaches MailHog's inbox anymore
- `api` — Spring Boot on port 8080, waits for `db`'s healthcheck

Two-stage Dockerfile: `maven:3.9-eclipse-temurin-25-alpine` build stage → `eclipse-temurin:25-jre-alpine` run stage (no Maven/source in the final image, ~200MB vs ~600MB).

Flyway runs automatically on boot (`spring.flyway.enabled: true`), migrations in `src/main/resources/db/migration/`. **Never edit an already-applied migration file** — Flyway checksums each file and fails startup on a mismatch; add a new `Vx` instead.

---

## 19. Flyway migration history

| File | What it does |
|---|---|
| V1 | All tables, ENUMs, indexes |
| V2 | Seed: 6 confederations, 50 countries, 16 stadiums |
| V3 | Seed: 48 teams (pre-draw) |
| V4 | Seed: full squad rosters (~700+ players) |
| V5 | Seed: 104-match schedule (placeholder dates/venues) |
| V6 | `email_verification_tokens` table |
| V7 | `comment_likes` table (repair migration) |
| V8 | Corrects the 48 qualified teams to the official Dec 2025 FIFA draw; fixes all group assignments |
| V9 | Replaces all 72 group-stage match rows with correct draw pairings, venues, and UTC kickoff times |
| V10 | `fix_match_schedule` — corrects 2 group-stage kickoffs; **replaces all 32 knockout match schedules/venues** with real dates (Round of 32 starts June 28, 2026, not the V5 placeholder) |
| V11 | `email_verification_otp` — drops the global per-token UNIQUE constraint, adds per-`(user_id, token)` UNIQUE so 6-digit OTP codes can repeat across different users |
| V12 | `community_and_tournament_scoring` — adds `tournament_result` table, `points_earned` on `tournament_predictions`, `tournament_points` on `prediction_leaderboard`, and the `community_posts` table |

---

## 20. Known gaps

1. **Knockout bracket auto-population** — `home_team_id`/`away_team_id` stay `NULL` for knockout matches until an admin manually sets them via `PATCH /api/admin/matches/{id}`. No logic advances winners automatically.
2. **Team/player admin CRUD** — no endpoints to edit teams or players; still requires direct SQL.
3. **Password change / reset** — no forgot-password or change-password flow exists.
4. **Test coverage** — no tests for `UserAdminController`, `CommunityController`, `CommunityPostController`, or the tournament-scoring endpoints.
5. **Stale local env config** — dead SMTP variables in `docker-compose.yml`/`.env`, missing `BREVO_API_KEY` (see [§16](#16-configuration-and-environment-variables)).
6. **`match_events` table** — exists in the schema (V1) but has no corresponding Java entity/repository; effectively dead.

Resolved since the last pass (kept here for history): knockout-stage placeholder dates (V10), tournament prediction scoring (V12 + `TournamentScoringController`).

---

## 21. Production incidents fixed

### Incident 1 — Admin score update returned 403 Forbidden

The UI (Vercel) proxies `/api/**` server-side to the Railway backend via a `vercel.json` rewrite. Vercel forwards the original `Origin` header, so Spring's CORS filter still validates the request — and `PATCH` was missing from the allowed methods list, so the filter rejected it with 403 before the JWT filter or authorization rules were ever reached (the admin's token/role were never actually checked).

**Fix:** added `"PATCH"` to `SecurityConfig.corsSource()`'s allowed methods.

### Incident 2 — Admin score update returned 500 (after Incident 1's fix)

`MatchRepository.updateResult()` used PostgreSQL's `::` shorthand cast in a native query: `SET status = :status::match_status`. Hibernate 7's named-parameter parser treats `:status::match_status` as one parameter named `status::match_status` (cast suffix included), not `:status` followed by a separate cast — so binding `@Param("status")` threw `UnknownParameterException`.

**Fix:** replaced `:status::match_status` with `CAST(:status AS match_status)`, which parses correctly and is behaviorally identical in PostgreSQL. Only this query was affected — other native queries either don't use `::` or only apply it to plain columns (e.g. `m.status::text`), not immediately after a named parameter.

### Incident 3 — Verification emails never arrived on Railway

Railway blocks outbound SMTP ports (587/465/2525), so the original JavaMailSender/Gmail-SMTP integration silently failed in production even though it worked locally against MailHog.

**Fix:** replaced SMTP entirely with Brevo's HTTPS transactional email API (`RestClient` → `https://api.brevo.com/v3/smtp/email`, `BREVO_API_KEY`). No SMTP protocol involved, so no ports to block. Requires a verified sender email in Brevo's dashboard (Senders & Domains → Add a Sender) — no custom domain purchase needed, any personal email can be verified as a sender.
