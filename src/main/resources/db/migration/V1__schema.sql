-- =============================================================
-- V1__schema.sql  –  2026 FIFA World Cup Platform
-- All tables, relationships, and indexes
-- =============================================================

-- ---------------------------------------------------------------
-- REFERENCE / LOOKUP TABLES
-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS confederations (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(10)  NOT NULL UNIQUE,   -- UEFA, CONMEBOL, CAF, AFC, CONCACAF, OFC
    name        VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS countries (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    code_iso2       CHAR(2)      NOT NULL UNIQUE,
    code_iso3       CHAR(3)      NOT NULL UNIQUE,
    flag_emoji      VARCHAR(10),
    confederation_id INT REFERENCES confederations(id)
);

-- ---------------------------------------------------------------
-- TOURNAMENT STRUCTURE
-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS stadiums (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    country     VARCHAR(100) NOT NULL,          -- USA, Canada, or Mexico
    capacity    INT,
    latitude    DECIMAL(9,6),
    longitude   DECIMAL(9,6)
);

CREATE TABLE IF NOT EXISTS teams (
    id              SERIAL PRIMARY KEY,
    country_id      INT          NOT NULL UNIQUE REFERENCES countries(id),
    fifa_ranking    INT,
    group_id        CHAR(1),                    -- A–L  (null until draw)
    manager         VARCHAR(100),
    kit_primary     VARCHAR(7),                 -- hex color
    kit_secondary   VARCHAR(7),
    qualified_at    DATE,
    is_host         BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS players (
    id              SERIAL PRIMARY KEY,
    team_id         INT          NOT NULL REFERENCES teams(id),
    name            VARCHAR(150) NOT NULL,
    date_of_birth   DATE,
    nationality     VARCHAR(100),
    position        VARCHAR(30) NOT NULL
                    CHECK (position IN ('GK','CB','LB','RB','LWB','RWB',
                                        'CDM','CM','CAM','LM','RM',
                                        'LW','RW','SS','ST','CF')),
    shirt_number    SMALLINT,
    club            VARCHAR(100),
    caps            INT DEFAULT 0,
    goals           INT DEFAULT 0,
    height_cm       SMALLINT,
    market_value_eur BIGINT,                    -- in euros
    is_captain      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- ---------------------------------------------------------------
-- MATCH SCHEDULE
-- ---------------------------------------------------------------

CREATE TYPE match_stage AS ENUM (
    'GROUP','ROUND_OF_32','ROUND_OF_16','QUARTER_FINAL',
    'SEMI_FINAL','THIRD_PLACE','FINAL'
);

CREATE TYPE match_status AS ENUM (
    'SCHEDULED','LIVE','FINISHED','POSTPONED','CANCELLED'
);

CREATE TABLE IF NOT EXISTS matches (
    id              SERIAL PRIMARY KEY,
    match_number    INT          NOT NULL UNIQUE, -- 1..104
    stage           match_stage  NOT NULL,
    group_id        CHAR(1),                      -- only for GROUP stage
    home_team_id    INT REFERENCES teams(id),     -- null for TBD knockout games
    away_team_id    INT REFERENCES teams(id),
    stadium_id      INT REFERENCES stadiums(id),
    scheduled_at    TIMESTAMPTZ  NOT NULL,
    status          match_status NOT NULL DEFAULT 'SCHEDULED',
    home_score      SMALLINT,
    away_score      SMALLINT,
    home_score_pen  SMALLINT,                     -- penalty shootout
    away_score_pen  SMALLINT,
    attendance      INT,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS match_events (
    id              SERIAL PRIMARY KEY,
    match_id        INT          NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    player_id       INT          REFERENCES players(id),
    event_type      VARCHAR(30)  NOT NULL
                    CHECK (event_type IN ('GOAL','OWN_GOAL','YELLOW_CARD',
                                          'RED_CARD','SUBSTITUTION','PENALTY_GOAL',
                                          'PENALTY_MISS','VAR_REVIEW')),
    minute          SMALLINT,
    extra_time      SMALLINT DEFAULT 0,
    detail          TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- ---------------------------------------------------------------
-- USERS & SOCIAL FEATURES
-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
    id              SERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(100),
    avatar_url      TEXT,
    country_id      INT REFERENCES countries(id),
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER'
                    CHECK (role IN ('USER','MODERATOR','ADMIN')),
    is_verified     BOOLEAN DEFAULT FALSE,
    is_active       BOOLEAN DEFAULT TRUE,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id              SERIAL PRIMARY KEY,
    user_id         INT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token           VARCHAR(512) NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ  NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_favorite_teams (
    user_id         INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    team_id         INT NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (user_id, team_id)
);

CREATE TABLE IF NOT EXISTS user_favorite_players (
    user_id         INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    player_id       INT NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (user_id, player_id)
);

-- ---------------------------------------------------------------
-- PREDICTIONS
-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS predictions (
    id                  SERIAL PRIMARY KEY,
    user_id             INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    match_id            INT NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    predicted_home_score SMALLINT NOT NULL,
    predicted_away_score SMALLINT NOT NULL,
    points_earned       SMALLINT DEFAULT 0,   -- 0/1/3 — calculated after match
    created_at          TIMESTAMPTZ DEFAULT NOW(),
    updated_at          TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, match_id)                -- one prediction per match per user
);

CREATE TABLE IF NOT EXISTS prediction_leaderboard (
    user_id         INT NOT NULL REFERENCES users(id) ON DELETE CASCADE PRIMARY KEY,
    total_points    INT NOT NULL DEFAULT 0,
    exact_scores    INT NOT NULL DEFAULT 0,
    correct_results INT NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

-- ---------------------------------------------------------------
-- COMMENTS / OPINIONS
-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS comments (
    id              SERIAL PRIMARY KEY,
    user_id         INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    -- polymorphic target: one of these is set
    match_id        INT REFERENCES matches(id) ON DELETE CASCADE,
    team_id         INT REFERENCES teams(id) ON DELETE CASCADE,
    player_id       INT REFERENCES players(id) ON DELETE CASCADE,
    parent_id       INT REFERENCES comments(id) ON DELETE CASCADE, -- threading
    body            TEXT NOT NULL,
    is_deleted      BOOLEAN DEFAULT FALSE,
    like_count      INT DEFAULT 0,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT one_target CHECK (
        (match_id IS NOT NULL)::int +
        (team_id  IS NOT NULL)::int +
        (player_id IS NOT NULL)::int = 1
    )
);

CREATE TABLE IF NOT EXISTS comment_likes (
    user_id     INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    comment_id  INT NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, comment_id)
);

-- ---------------------------------------------------------------
-- TOURNAMENT WINNER PREDICTION (fun extra)
-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS tournament_predictions (
    id              SERIAL PRIMARY KEY,
    user_id         INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    predicted_winner_id  INT REFERENCES teams(id),
    predicted_runner_up  INT REFERENCES teams(id),
    predicted_top_scorer INT REFERENCES players(id),
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id)
);

-- ---------------------------------------------------------------
-- INDEXES  (critical for query performance)
-- ---------------------------------------------------------------

-- Teams / Players
CREATE INDEX IF NOT EXISTS idx_players_team       ON players(team_id);
CREATE INDEX IF NOT EXISTS idx_players_position   ON players(position);
CREATE INDEX IF NOT EXISTS idx_players_club       ON players(club);

-- Matches
CREATE INDEX IF NOT EXISTS idx_matches_stage      ON matches(stage);
CREATE INDEX IF NOT EXISTS idx_matches_group      ON matches(group_id);
CREATE INDEX IF NOT EXISTS idx_matches_scheduled  ON matches(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_matches_status     ON matches(status);
CREATE INDEX IF NOT EXISTS idx_matches_home_team  ON matches(home_team_id);
CREATE INDEX IF NOT EXISTS idx_matches_away_team  ON matches(away_team_id);

-- Match events
CREATE INDEX IF NOT EXISTS idx_events_match       ON match_events(match_id);
CREATE INDEX IF NOT EXISTS idx_events_player      ON match_events(player_id);

-- Users
CREATE INDEX IF NOT EXISTS idx_users_email        ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username     ON users(username);

-- Predictions
CREATE INDEX IF NOT EXISTS idx_predictions_user   ON predictions(user_id);
CREATE INDEX IF NOT EXISTS idx_predictions_match  ON predictions(match_id);

-- Comments
CREATE INDEX IF NOT EXISTS idx_comments_match     ON comments(match_id) WHERE match_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_comments_team      ON comments(team_id)  WHERE team_id  IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_comments_player    ON comments(player_id) WHERE player_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_comments_parent    ON comments(parent_id) WHERE parent_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_comments_user      ON comments(user_id);

-- Favorites
CREATE INDEX IF NOT EXISTS idx_fav_teams_user     ON user_favorite_teams(user_id);
CREATE INDEX IF NOT EXISTS idx_fav_players_user   ON user_favorite_players(user_id);

-- Leaderboard
CREATE INDEX IF NOT EXISTS idx_leaderboard_points ON prediction_leaderboard(total_points DESC);
