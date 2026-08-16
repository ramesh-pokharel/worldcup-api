-- Tournament scoring: admin sets actual outcome once after the final
CREATE TABLE IF NOT EXISTS tournament_result (
    id                   SERIAL PRIMARY KEY,
    actual_winner_id     INT REFERENCES teams(id),
    actual_top_scorer_id INT REFERENCES players(id),
    set_at               TIMESTAMPTZ DEFAULT NOW()
);

-- Store points earned per tournament prediction
ALTER TABLE tournament_predictions
    ADD COLUMN IF NOT EXISTS points_earned SMALLINT DEFAULT NULL;

-- Leaderboard: track tournament points separately for display
ALTER TABLE prediction_leaderboard
    ADD COLUMN IF NOT EXISTS tournament_points INT NOT NULL DEFAULT 0;

-- Community posts: free-form global discussion
CREATE TABLE IF NOT EXISTS community_posts (
    id         SERIAL PRIMARY KEY,
    user_id    INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    body       TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_community_posts_created ON community_posts(created_at DESC);
